package uk.cam.lib.cdl.loading;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import uk.cam.lib.cdl.loading.annotations.CanEditWorkspace;
import uk.cam.lib.cdl.loading.apis.EditAPI;
import uk.cam.lib.cdl.loading.dao.UserRepository;
import uk.cam.lib.cdl.loading.dao.WorkspaceRepository;
import uk.cam.lib.cdl.loading.forms.UserForm;
import uk.cam.lib.cdl.loading.forms.WorkspaceForm;
import uk.cam.lib.cdl.loading.model.editor.Workspace;
import uk.cam.lib.cdl.loading.model.security.Role;
import uk.cam.lib.cdl.loading.model.security.User;
import uk.cam.lib.cdl.loading.utils.RoleHelper;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.security.InvalidParameterException;
import java.util.List;

@Controller
public class UserManagementController {

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private UserRepository userRepository;

    private final EditAPI editAPI;

    @Autowired
    public UserManagementController(EditAPI editAPI) {
        this.editAPI = editAPI;
    }

    @GetMapping("/user-management/")
    @PreAuthorize("@roleService.hasRoleRegex(\"ROLE_SITE_MANAGER\", authentication) or " +
        "          @roleService.hasRoleRegex(\"ROLE_WORKSPACE_MANAGER\\d+\", authentication)")
    public String usermanagement(Model model, HttpServletRequest request) {

        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("workspaces", workspaceRepository.findAll());
        return "user-management";
    }

    @RequestMapping(value = {"/user-management/user/edit"})
    @PreAuthorize("@roleService.hasRoleRegex(\"ROLE_SITE_MANAGER\", authentication) or " +
        "          @roleService.hasRoleRegex(\"ROLE_WORKSPACE_MANAGER\\d+\", authentication)")
    public String updateUsers(Model model, @RequestParam(required = false, name = "id") Long id) {

        // TODO separate out roles to display and set

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

        model.addAttribute("form", form);
        model.addAttribute("allRoles", allRoles);
        return "user-management-user";
    }

    @PostMapping(value = {"/user-management/user/update"})
    @PreAuthorize("@roleService.hasRoleRegex(\"ROLE_SITE_MANAGER\", authentication) or " +
        "          @roleService.hasRoleRegex(\"ROLE_WORKSPACE_MANAGER\\d+\", authentication)")
    @Transactional
    public RedirectView updateUserFromForm(RedirectAttributes attributes,
                                           @Valid @ModelAttribute UserForm userForm,
                                           final BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            attributes.addFlashAttribute("error", "There was a problem saving your changes. See form below for " +
                "details.");
            attributes.addFlashAttribute("org.springframework.validation.BindingResult.form", bindingResult);
            attributes.addFlashAttribute("form", userForm);
            attributes.addAttribute("id", userForm.getId());

            return new RedirectView("/user-management/user/edit");
        }
        // TODO separate out roles to display and set

        User user = userForm.toUser();
        User userFromRepo = userRepository.findById(user.getId());
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
    @PreAuthorize("@roleService.hasRoleRegex(\"ROLE_SITE_MANAGER\", authentication) or " +
        "          @roleService.hasRoleRegex(\"ROLE_WORKSPACE_MANAGER\\d+\", authentication)")
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
    @CanEditWorkspace // requires workspaceIds or workspaceId or workspaceForm
    public String updateWorkspace(Model model, @RequestParam(required = false, name = "id") Long workspaceId) {

        // TODO only allow site manager to add workspace

        Workspace workspace;
        WorkspaceForm form = new WorkspaceForm();

        if (workspaceId != null) {
            workspace = workspaceRepository.findWorkspaceById(workspaceId);
            if (workspace != null) {
                form = new WorkspaceForm(workspace);
            }
        }

        model.addAttribute("form", form);
        model.addAttribute("allCollections", editAPI.getCollections());
        model.addAttribute("allItems", editAPI.getItems());
        return "user-management-workspace";
    }

    @PostMapping(value = {"/user-management/workspace/update"})
    @Transactional
    @CanEditWorkspace // requires workspaceIds workspaceId or workspaceForm
    public RedirectView updateWorkspaceFromForm(RedirectAttributes attributes,
                                                @Valid @ModelAttribute WorkspaceForm workspaceForm,
                                                final BindingResult bindingResult) {

        // TODO only allow site manager to add workspace

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
            workspaceRepository.save(workspace);
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

    @PostMapping(value = {"/user-management/workspace/delete"})
    @Transactional
    @Secured("ROLE_SITE_MANAGER")
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
