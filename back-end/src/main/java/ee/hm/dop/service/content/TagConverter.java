package ee.hm.dop.service.content;

import ee.hm.dop.dao.LearningObjectDao;
import ee.hm.dop.model.*;
import ee.hm.dop.model.taxon.Taxon;
import ee.hm.dop.service.content.dto.TagDTO;
import ee.hm.dop.service.metadata.ResourceTypeService;
import ee.hm.dop.service.metadata.TargetGroupService;
import ee.hm.dop.service.metadata.TaxonService;
import ee.hm.dop.service.reviewmanagement.ReviewableChangeService;
import ee.hm.dop.service.solr.SolrEngineService;
import org.joda.time.DateTime;

import javax.inject.Inject;
import java.util.List;

public class TagConverter {
    public static final String TAXON = "taxon";
    public static final String RESOURCETYPE = "resourcetype";
    public static final String TARGETGROUP = "targetgroup";
    @Inject
    private SolrEngineService solrEngineService;
    @Inject
    private TaxonService taxonService;
    @Inject
    private TargetGroupService targetGroupService;
    @Inject
    private ResourceTypeService resourceTypeService;
    @Inject
    private ReviewableChangeService reviewableChangeService;
    @Inject
    private LearningObjectDao learningObjectDao;

    public TagDTO addChangeReturnTagDto(String tagName, LearningObject learningObject, User user) {
        TagDTO tagDTO = new TagDTO();

        ReviewableChange reviewableChange = new ReviewableChange();
        reviewableChange.setLearningObject(learningObject);
        reviewableChange.setCreatedBy(user);
        reviewableChange.setCreatedAt(DateTime.now());

        Taxon taxon = taxonService.findTaxonByTranslation(tagName);
        ResourceType resourceType = resourceTypeService.findResourceByTranslation(tagName);
        TargetGroup targetGroup = targetGroupService.getByTranslation(tagName);

        boolean hasChanged = false;
        if (taxon != null) {
            addTaxon(learningObject, taxon);
            reviewableChange.setTaxon(taxon);
            tagDTO.setTagTypeName(TAXON);
            hasChanged = true;
        } else if (learningObject instanceof Material && resourceType != null) {
            Material material = (Material) learningObject;
            material.getResourceTypes().add(resourceType);
            reviewableChange.setResourceType(resourceType);
            tagDTO.setTagTypeName(RESOURCETYPE);
            hasChanged = true;
        } else if (targetGroup != null) {
            learningObject.getTargetGroups().add(targetGroup);
            reviewableChange.setTargetGroup(targetGroup);
            tagDTO.setTagTypeName(TARGETGROUP);
            hasChanged = true;
        }

        if (learningObject.getUnReviewed() == 0) {
            reviewableChangeService.addChanged(reviewableChange);
        }

        LearningObject updatedLearningObject = learningObjectDao.createOrUpdate(learningObject);
        updatedLearningObject.setChanged(hasChanged ? updatedLearningObject.getChanged() + 1 : updatedLearningObject.getChanged());
        tagDTO.setLearningObject(updatedLearningObject);

        solrEngineService.updateIndex();

        return tagDTO;
    }

    private void addTaxon(LearningObject learningObject, Taxon taxon) {
        List<Taxon> learningObjectTaxons = learningObject.getTaxons();
        if (learningObjectTaxons != null) {
            learningObjectTaxons.add(taxon);
        }
    }

}
