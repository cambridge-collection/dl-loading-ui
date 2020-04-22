package uk.cam.lib.cdl.loading.security.tags;

import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.processor.IProcessor;

import java.util.HashSet;
import java.util.Set;

public class RoleDialect extends AbstractProcessorDialect {

    public RoleDialect() {
        super(
            "UI Loading Dialect",    // Dialect name
            "dl-loading-ui",            // Dialect prefix (dl-loading-ui:*)
            1000);              // Dialect precedence
    }

    public Set<IProcessor> getProcessors(final String dialectPrefix) {
        final Set<IProcessor> processors = new HashSet<>();
        processors.add(new RoleAttributeTagProcessor(dialectPrefix));
        return processors;
    }


}
