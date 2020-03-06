package uk.cam.lib.cdl.loading;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import uk.cam.lib.cdl.loading.apis.EditAPI;
import uk.cam.lib.cdl.loading.dao.UserRepository;
import uk.cam.lib.cdl.loading.dao.WorkspaceRepository;
import uk.cam.lib.cdl.loading.forms.UserForm;
import uk.cam.lib.cdl.loading.forms.WorkspaceForm;
import uk.cam.lib.cdl.loading.model.editor.Item;
import uk.cam.lib.cdl.loading.model.editor.Workspace;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;


@Controller
public class UserManagementController {

    private final EditAPI editAPI;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private UserRepository userRepository;

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

    @GetMapping(value = {"/user-management/updateworkspace/{id}"})
    public String updateWorkspace (Model model, @PathVariable("id") long id) {

        Workspace workspace = workspaceRepository.findWorkspaceById(id);
        WorkspaceForm form;
        if (workspace!=null) {
            form = new WorkspaceForm(workspace);
        } else {
            form = new WorkspaceForm();
        }

        model.addAttribute("form", form);
        return "user-management-updateworkspace";
    }

}

