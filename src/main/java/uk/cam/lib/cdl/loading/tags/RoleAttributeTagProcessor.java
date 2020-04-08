package uk.cam.lib.cdl.loading.tags;

import org.springframework.security.core.Authentication;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.extras.springsecurity5.util.SpringSecurityContextUtils;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;
import uk.cam.lib.cdl.loading.security.RoleService;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        if (attributeValue.equals("EnableIfCanViewWorkspaces")) {
            if (roleService.canViewWorkspaces(authentication)) {
                structureHandler.setAttribute("class", classAttrNotDisabled);
            } else {
                structureHandler.setAttribute("class", "disabled "+classAttrNotDisabled);
            }
        }

        if (attributeValue.equals("EnableIfCanDeploySites") ||
            attributeValue.equals("EnableIfCanBuildPackages")) {
            if (roleService.canDeploySites(authentication)) {
               structureHandler.setAttribute("class", classAttrNotDisabled);
            } else {
                structureHandler.setAttribute("class", "disabled "+classAttrNotDisabled);
            }
        }

        Pattern p = Pattern.compile("EnableIfCanEditWorkspace(\\d+)");
        Matcher m = p.matcher(attributeValue);
        if (m.find()) {

            String workspaceId = m.toMatchResult().group(0).replace("EnableIfCanEditWorkspace", "");
            if (roleService.canEditWorkspace(Long.valueOf(workspaceId), authentication)) {
                structureHandler.setAttribute("class", classAttrNotDisabled);
            } else {
                structureHandler.setAttribute("class", "disabled "+classAttrNotDisabled);
            }
        }

        if (attributeValue.equals("EnableIfCanEditWorkspaces")) {
            if (roleService.canEditWorkspaces(authentication)) {
                structureHandler.setAttribute("class", classAttrNotDisabled);
            } else {
                structureHandler.setAttribute("class", "disabled "+classAttrNotDisabled);
            }
        }

        if (attributeValue.equals("EnableIfCanAddWorkspaces")) {
            if (roleService.canAddWorkspaces(authentication)) {
                structureHandler.setAttribute("class", classAttrNotDisabled);
            } else {
                structureHandler.setAttribute("class", "disabled "+classAttrNotDisabled);
            }
        }
    }

}
