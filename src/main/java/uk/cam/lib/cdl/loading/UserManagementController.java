package uk.cam.lib.cdl.loading;

import org.springframework.beans.factory.annotation.Autowired;
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
import uk.cam.lib.cdl.loading.model.editor.Workspace;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

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
    public String usermanagement(Model model, HttpServletRequest request) {

        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("workspaces", workspaceRepository.findAll());
        return "user-management";
    }

    @GetMapping(value = {"/user-management/adduser/"})
    public String addUser(Model model) {

        UserForm form = (UserForm) model.asMap().get("form");
        if (form == null) {
            form = new UserForm();
        }

        model.addAttribute("form", form);
        return "user-management-adduser";
    }

    @RequestMapping(value = {"/user-management/workspace/edit"})
    public String updateWorkspace(Model model, @RequestParam(required = false, name = "id") Long id) {

        Workspace workspace;
        WorkspaceForm form = new WorkspaceForm();

        if (id != null) {
            workspace = workspaceRepository.findWorkspaceById(id);
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
}
