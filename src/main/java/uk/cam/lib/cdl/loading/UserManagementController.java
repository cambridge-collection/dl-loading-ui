package uk.cam.lib.cdl.loading;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import uk.cam.lib.cdl.loading.apis.EditAPI;
import uk.cam.lib.cdl.loading.dao.UserRepository;
import uk.cam.lib.cdl.loading.dao.WorkspaceRepository;
import uk.cam.lib.cdl.loading.forms.UserForm;
import uk.cam.lib.cdl.loading.forms.WorkspaceForm;
import uk.cam.lib.cdl.loading.model.editor.Collection;
import uk.cam.lib.cdl.loading.model.editor.Item;
import uk.cam.lib.cdl.loading.model.editor.Workspace;
import uk.cam.lib.cdl.loading.model.security.Role;
import uk.cam.lib.cdl.loading.model.security.User;
import uk.cam.lib.cdl.loading.utils.RoleHelper;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class UserManagementController {

    private WorkspaceRepository workspaceRepository;

    private UserRepository userRepository;

    private final EditAPI editAPI;

    private ApplicationContext appContext;

    @Autowired
    public UserManagementController(EditAPI editAPI, UserRepository userRepository,
                                    WorkspaceRepository workspaceRepository, ApplicationContext
                                    context) {
        this.editAPI = editAPI;
        this.userRepository = userRepository;
        this.workspaceRepository = workspaceRepository;
        this.appContext = context;
    }

    @GetMapping("/user-management/")
    @PreAuthorize("@roleService.canEditWorkspaces(authentication)")
    public String usermanagement(Model model, HttpServletRequest request) {

        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("workspaces", workspaceRepository.findAll());
        return "user-management";
    }

    @RequestMapping(value = {"/user-management/user/edit"})
    @PreAuthorize("@roleService.canEditWorkspaces(authentication)")
    public String updateUsers(Model model, @RequestParam(required = false, name = "id") Long id,
                              Authentication authentication) {

        User user;
        UserForm form = new UserForm();

        if (id != null) {
            user = userRepository.findById(id.longValue());
            if (user != null) {
                form = new UserForm(user);
            }
        }

        RoleHelper roleHelper = new RoleHelper(workspaceRepository);
        List<Role> allRoles = roleHelper.getAllRoles();
        List<Role> roles = roleHelper.getRolesUserCanAssign(authentication);
        model.addAttribute("form", form);
        model.addAttribute("allRoles", allRoles);
        model.addAttribute("roles", roles);
        return "user-management-user";
    }

    @PostMapping(value = {"/user-management/user/update"})
    @PreAuthorize("@roleService.canEditWorkspaces(authentication)")
    @Transactional
    public RedirectView updateUserFromForm(RedirectAttributes attributes,
                                           @Valid @ModelAttribute UserForm userForm,
                                           final BindingResult bindingResult,
                                           Authentication authentication) {

        if (bindingResult.hasErrors()) {
            attributes.addFlashAttribute("error", "There was a problem saving your changes. See form below for " +
                "details.");
            attributes.addFlashAttribute("org.springframework.validation.BindingResult.form", bindingResult);
            attributes.addFlashAttribute("form", userForm);
            attributes.addAttribute("id", userForm.getId());

            return new RedirectView("/user-management/user/edit");
        }

        RoleHelper roleHelper = new RoleHelper(workspaceRepository);
        User user = userForm.toUser();
        User userFromRepo = userRepository.findById(user.getId());

        // Ensure user has permission to set roles
        List<Role> allowedRoles = roleHelper.getRolesUserCanAssign(authentication);
        List<String> allowedAuthorities = new ArrayList<>();
        for (Role role: roleHelper.getAllRoles()) {
            if (allowedRoles.contains(role) && user.getAuthorities().contains(role.getName())) {
                allowedAuthorities.add(role.getName());
            } else
            if (!allowedRoles.contains(role) && userFromRepo!=null && userFromRepo.getAuthorities().contains(role.getName())) {
                allowedAuthorities.add(role.getName());
            }
        }

        user.setAuthorities(allowedAuthorities);

        if (userFromRepo == null) {
            userRepository.save(user);
        } else {
            userFromRepo.setUsername(user.getUsername());
            userFromRepo.setFirstName(user.getFirstName());
            userFromRepo.setLastName(user.getLastName());
            userFromRepo.setEmail(user.getEmail());
            userFromRepo.setEnabled(user.isEnabled());
            userFromRepo.setAuthorities(user.getAuthorities());
            userRepository.save(userFromRepo);
        }

        attributes.addFlashAttribute("form", userForm);
        attributes.addAttribute("id", userForm.getId());
        return new RedirectView("/user-management/user/edit");
    }

    @PostMapping(value = {"/user-management/user/delete"})
    @PreAuthorize("@roleService.canEditWorkspaces(authentication)")
    @Transactional
    public RedirectView deleteUser(@RequestParam("id") Long id) {
        User userFromRepo = userRepository.findById(id.longValue());
        if (userFromRepo != null) {
            userRepository.delete(userFromRepo);
        } else {
            throw new InvalidParameterException("Unknown user id: " + id);
        }
        return new RedirectView("/user-management/");
    }


    @RequestMapping(value = {"/user-management/workspace/edit"})
    @PreAuthorize("@roleService.canEditWorkspace(#workspaceId, authentication)")
    public String updateWorkspace(Model model, @RequestParam(required = false, name = "id") Long workspaceId) {

        Workspace workspace;
        WorkspaceForm form = null;

        if (workspaceId != null) {
            workspace = workspaceRepository.findWorkspaceById(workspaceId);
            if (workspace != null) {
                form = new WorkspaceForm(workspace);
            }
        }

        // New workspace
        if (form==null) {
            return appContext.getBean(UserManagementController.class).addWorkspace(model,
                editAPI.getCollections(), editAPI.getItems());
        }

        model.addAttribute("form", form);
        model.addAttribute("allCollections", editAPI.getCollections());
        model.addAttribute("allItems", editAPI.getItems());
        return "user-management-workspace";
    }


    @PreAuthorize("@roleService.canAddWorkspaces(authentication)")
    private String addWorkspace(Model model, Iterable<Collection> collections, Iterable<Item> items ) {
        model.addAttribute("form", new WorkspaceForm());
        model.addAttribute("allCollections", collections);
        model.addAttribute("allItems", items);
        return "user-management-workspace";
    }

    @PostMapping(value = {"/user-management/workspace/update"})
    @Transactional
    @PreAuthorize("@roleService.canEditWorkspace(#workspaceForm.id, authentication)")
    public RedirectView updateWorkspaceFromForm(RedirectAttributes attributes,
                                                @Valid @ModelAttribute WorkspaceForm workspaceForm,
                                                final BindingResult bindingResult) {

       if (bindingResult.hasErrors()) {
            attributes.addFlashAttribute("error", "There was a problem saving your changes. See form below for " +
                "details.");
            attributes.addFlashAttribute("org.springframework.validation.BindingResult.form", bindingResult);
            attributes.addFlashAttribute("form", workspaceForm);
            attributes.addAttribute("id", workspaceForm.getId());

            return new RedirectView("/user-management/workspace/edit");
        }

        Workspace workspace = workspaceForm.toWorkspace();
        Workspace workspaceFromRepo = workspaceRepository.findWorkspaceById(workspace.getId());
        if (workspaceFromRepo == null) {
            // New Workspace
            return appContext.getBean(UserManagementController.class).addWorkspaceFromForm(attributes, workspace, workspaceForm);
        } else {
            workspaceFromRepo.setName(workspace.getName());
            workspaceFromRepo.setCollectionIds(workspace.getCollectionIds());
            workspaceFromRepo.setItemIds(workspace.getItemIds());
            workspaceRepository.save(workspaceFromRepo);
        }

        attributes.addFlashAttribute("form", workspaceForm);
        attributes.addAttribute("id", workspaceForm.getId());
        return new RedirectView("/user-management/workspace/edit");
    }

    @Transactional
    @PreAuthorize("@roleService.canAddWorkspaces(authentication)")
    protected RedirectView addWorkspaceFromForm(RedirectAttributes attributes, Workspace workspace,
                                                WorkspaceForm workspaceForm) {
        workspaceRepository.save(workspace);
        attributes.addFlashAttribute("form", workspaceForm);
        attributes.addAttribute("id", workspaceForm.getId());
        return new RedirectView("/user-management/workspace/edit");

    }

    @PostMapping(value = {"/user-management/workspace/delete"})
    @Transactional
    @PreAuthorize("@roleService.canAddWorkspaces(authentication)")
    public RedirectView deleteWorkspace(@RequestParam("id") Long id) {
        Workspace workspaceFromRepo = workspaceRepository.findWorkspaceById(id);
        if (workspaceFromRepo != null) {
            workspaceRepository.delete(workspaceFromRepo);
        } else {
            throw new InvalidParameterException("Unknown workspace id: " + id);
        }
        return new RedirectView("/user-management/");
    }

}
