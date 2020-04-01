package uk.cam.lib.cdl.loading.annotations;

import org.springframework.security.core.Authentication;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.extras.springsecurity5.util.SpringSecurityContextUtils;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;
import uk.cam.lib.cdl.loading.security.RoleService;

public class RoleAttributeTagProcessor extends AbstractAttributeTagProcessor {

    private static final String ATTR_NAME = "role";
    private static final int PRECEDENCE = 10000;
    private static final RoleService roleService = new RoleService();


    public RoleAttributeTagProcessor(final String dialectPrefix) {
        super(
            TemplateMode.HTML, // This processor will apply only to HTML mode
            dialectPrefix,     // Prefix to be applied to name for matching
            null,              // No tag name: match any tag name
            false,             // No prefix to be applied to tag name
            ATTR_NAME,         // Name of the attribute that will be matched
            true,              // Apply dialect prefix to attribute name
            PRECEDENCE,        // Precedence (inside dialect's precedence)
            true);             // Remove the matched attribute afterwards
    }


    protected void doProcess(
        final ITemplateContext context, final IProcessableElementTag tag,
        final AttributeName attributeName, final String attributeValue,
        final IElementTagStructureHandler structureHandler) {

        Authentication authentication = SpringSecurityContextUtils.getAuthenticationObject(context);

        String classAttr, classAttrNotDisabled = "";
        if (tag.getAttribute("class")!=null) {
            classAttr = tag.getAttribute("class").getValue();
            classAttrNotDisabled = classAttr.replaceAll("\\s*disabled\\s*", "");
        }

        if (attributeValue.equals("EnableIfRoleWorkspaceMemberOrManager")) {
            if (roleService.hasRoleRegex("ROLE_WORKSPACE_MEMBER\\d+", authentication) ||
                roleService.hasRoleRegex("ROLE_WORKSPACE_MANAGER\\d+", authentication)) {
                structureHandler.setAttribute("class", classAttrNotDisabled);
            } else {
                structureHandler.setAttribute("class", "disabled "+classAttrNotDisabled);
            }
        }

        if (attributeValue.equals("EnableIfRoleDeploymentManager")) {
            if (roleService.hasRoleRegex("ROLE_DEPLOYMENT_MANAGER", authentication)) {
               structureHandler.setAttribute("class", classAttrNotDisabled);
            } else {
                structureHandler.setAttribute("class", "disabled "+classAttrNotDisabled);
            }
        }

        if (attributeValue.equals("EnableIfRoleSiteManagerOrWorkspaceManager")) {
            if (roleService.hasRoleRegex("ROLE_SITE_MANAGER", authentication) ||
                roleService.hasRoleRegex("ROLE_WORKSPACE_MANAGER\\d+", authentication)) {
                structureHandler.setAttribute("class", classAttrNotDisabled);
            } else {
                structureHandler.setAttribute("class", "disabled "+classAttrNotDisabled);
            }
        }
    }

}
