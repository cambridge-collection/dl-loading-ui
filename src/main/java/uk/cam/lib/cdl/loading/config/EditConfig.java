package uk.cam.lib.cdl.loading.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import uk.cam.lib.cdl.loading.apis.EditAPI;
import uk.cam.lib.cdl.loading.apis.EditAPIUpdater;
import uk.cam.lib.cdl.loading.editing.modelcreation.DefaultModelFactory.IdCreationStrategy;
import uk.cam.lib.cdl.loading.editing.modelcreation.ModelFactory;
import uk.cam.lib.cdl.loading.editing.modelcreation.itemcreation.TeiIdCreationStrategy;
import uk.cam.lib.cdl.loading.editing.modelcreation.itemcreation.TeiItemFactory;
import uk.cam.lib.cdl.loading.editing.pagination.DefaultTEIPageConverter;
import uk.cam.lib.cdl.loading.editing.pagination.IIIFImageQuerier;
import uk.cam.lib.cdl.loading.editing.pagination.ImmutableCSVPageLoader;
import uk.cam.lib.cdl.loading.editing.pagination.TeiPageListFactory;
import uk.cam.lib.cdl.loading.exceptions.EditApiException;
import uk.cam.lib.cdl.loading.model.editor.Item;
import uk.cam.lib.cdl.loading.model.editor.ModelOps;
import uk.cam.lib.cdl.loading.model.editor.UI;
import uk.cam.lib.cdl.loading.model.editor.ui.UIThemeData;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Configuration
@EnableScheduling
public class EditConfig {
    @Bean
    public Path dlDatasetFilename(@Value("${data.dl-dataset.filename}") Path dlDatasetFilename) {
        return dlDatasetFilename;
    }

    @Bean
    public Path dlUIFilename(@Value("${data.ui.filename}") Path dlUIFilename) {
        return dlUIFilename;
    }

    @Bean Path dataItemPath(@Value("${data.item.path}") Path dataItemPath) {
        if(dataItemPath.isAbsolute()) {
            throw new IllegalArgumentException(String.format("dataItemPath cannot be absolute: '%s'", dataItemPath));
        }
        return dataItemPath;
    }

    @Bean Path absoluteDataItemPath(@Qualifier("dataRoot") Path dataRoot, @Qualifier("dataItemPath") Path dataItemPath) {
        Preconditions.checkArgument(dataRoot.isAbsolute());
        Preconditions.checkArgument(!dataItemPath.isAbsolute());
        var absoluteDataItemPath = dataRoot.resolve(dataItemPath).normalize();
        Preconditions.checkArgument(absoluteDataItemPath.startsWith(dataRoot));
        return absoluteDataItemPath;
    }

    @Bean Path dataRoot(@Value("${git.sourcedata.checkout.path}") Path gitRepoRoot,
                        @Value("${git.sourcedata.checkout.subpath.data}") Path gitRepoDataSubpath) {
        Preconditions.checkArgument(gitRepoRoot.isAbsolute());
        Preconditions.checkArgument(!gitRepoDataSubpath.isAbsolute());
        var dataRoot = gitRepoRoot.resolve(gitRepoDataSubpath).normalize();
        Preconditions.checkArgument(dataRoot.startsWith(gitRepoRoot));
        return dataRoot;
    }

    @Bean( name = "iiifImageServer")
    public IIIFImageQuerier iiifImageQuerier(@Value("${iiif-image-server}") String iiifImageServer) {
        return new IIIFImageQuerier(iiifImageServer);
    }

    @Bean
    @Profile("!test")
    public EditAPI editAPI(Path dataRoot, Path dlDatasetFilename,
                           Path dlUIFilename, Path absoluteDataItemPath) throws EditApiException {
        return new EditAPI(
            dataRoot.toString(),
            dlDatasetFilename.toString(),
            dlUIFilename.toString(),
            absoluteDataItemPath.toString());
    }

    @Bean
    public IdCreationStrategy teiIdCreationStrategy(Path dataItemPath) {
        return TeiIdCreationStrategy.builder()
            .baseTeiItemPath(dataItemPath)
            .build();
    }

    /**
     * Tags to be added to each {@code <graphic>} element's {@code decls} attribute when generating TEI pagination.
     *
     * <p>e.g:</p>
     *
     * <p><pre>{@code
     * <surface n="6" xml:id="i8">
     *   <graphic decls="#downloadImageRights #download"
     *     height="7428px"
     *     width="4734px"
     *     rend="portrait"
     *     url="PR-WADDLETON-B-00021-00005-000-00008"/>
     * </surface>
     * }</pre></p>
     */
    @Bean
    public List<String> teiPaginationGraphicDeclTags() {
        return ImmutableList.of("#downloadImageRights", "#download");
    }

    @Bean
    public TeiPageListFactory teiPageListFactory(List<String> teiPaginationGraphicDeclTags) {
        return TeiPageListFactory.builder()
            .pageLoader(ImmutableCSVPageLoader.builder().build())
            .teiPageConverter(new DefaultTEIPageConverter(teiPaginationGraphicDeclTags))
            .build();
    }

    /**
     * The {@link ModelFactory} responsible for constructing {@link Item} instances from user-provided TEI.
     */
    @Bean
    public TeiItemFactory teiItemFactory(IdCreationStrategy teiIdCreationStrategy,
                                         TeiPageListFactory teiPageListFactory) {
        return TeiItemFactory.builder()
            .teiIdCreationStrategy(teiIdCreationStrategy)
            .teiPageListFactory(teiPageListFactory)
            .build();
    }

    @ConditionalOnProperty(
        value = "edit.scheduling.enable", havingValue = "true", matchIfMissing = true
    )
    @Bean
    public EditAPIUpdater editAPIUpdater(EditAPI editAPI) {
        return new EditAPIUpdater(editAPI);
    }

    @Bean(name = "ModelOps")
    public ModelOps modelOps() {
        return ModelOps.ModelOps();
    }

    @Bean
    @Profile("!test")
    public UIThemeData uiThemeData(EditAPI editAPI) throws IOException {

        if (editAPI==null || editAPI.getUiFile()==null) {return null;}

        // Setup for reading json5
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(JsonParser.Feature.ALLOW_COMMENTS);
        mapper.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        mapper.enable(JsonReadFeature.ALLOW_TRAILING_COMMA.mappedFeature());
        mapper.enable(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES.mappedFeature());
        mapper.enable(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER.mappedFeature());
        mapper.enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS.mappedFeature());
        mapper.enable(JsonReadFeature.ALLOW_JAVA_COMMENTS.mappedFeature());
        mapper.enable(JsonReadFeature.ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS.mappedFeature());

        UI ui = mapper.readValue(editAPI.getUiFile().toFile(), UI.class);
        return ui.getThemeData();

    }
}

