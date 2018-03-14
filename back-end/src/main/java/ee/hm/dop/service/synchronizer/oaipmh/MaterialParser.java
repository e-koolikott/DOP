package ee.hm.dop.service.synchronizer.oaipmh;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Inject;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import ee.hm.dop.model.*;
import ee.hm.dop.model.taxon.Domain;
import ee.hm.dop.model.taxon.EducationalContext;
import ee.hm.dop.model.taxon.Module;
import ee.hm.dop.model.taxon.Specialization;
import ee.hm.dop.model.taxon.Subject;
import ee.hm.dop.model.taxon.Subtopic;
import ee.hm.dop.model.taxon.Taxon;
import ee.hm.dop.model.taxon.Topic;
import ee.hm.dop.service.useractions.PeerReviewService;
import ee.hm.dop.service.author.AuthorService;
import ee.hm.dop.service.author.PublisherService;
import ee.hm.dop.service.metadata.LanguageService;
import ee.hm.dop.service.metadata.ResourceTypeService;
import ee.hm.dop.service.metadata.TagService;
import ee.hm.dop.service.metadata.TargetGroupService;
import ezvcard.Ezvcard;
import ezvcard.VCard;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static ee.hm.dop.service.synchronizer.oaipmh.MaterialParserUtil.notEmpty;

public abstract class MaterialParser {

    public static final String TAXON_PATH = "./*[local-name()='taxonPath']";
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String[] SCHEMES = {"http", "https"};
    public static final String PUBLISHER = "PUBLISHER";
    public static final String AUTHOR = "AUTHOR";
    private static final Map<String, String> taxonMap;

    static {
        taxonMap = new HashMap<>();
        taxonMap.put("preschoolTaxon", "preschoolEducation");
        taxonMap.put("basicSchoolTaxon", "basicEducation");
        taxonMap.put("gymnasiumTaxon", "secondaryEducation");
        taxonMap.put("vocationalTaxon", "vocationalEducation");
    }

    protected XPath xpath = XPathFactory.newInstance().newXPath();

    @Inject
    private ResourceTypeService resourceTypeService;
    @Inject
    private PeerReviewService peerReviewService;
    @Inject
    private PublisherService publisherService;
    @Inject
    private AuthorService authorService;
    @Inject
    private TargetGroupService targetGroupService;

    public Material parse(Document doc) throws ParseException {
        try {
            Material material = new Material();
            doc.getDocumentElement().normalize();

            setIdentifier(material, doc);
            setContributorsData(material, doc);
            setTitles(material, doc);
            setLanguage(material, doc);
            setDescriptions(material, doc);
            setSource(material, doc);
            setTags(material, doc);
            setLearningResourceType(material, doc);
            setPeerReview(material, doc);
            setTaxon(material, doc);
            setCrossCurricularThemes(material, doc);
            setKeyCompetences(material, doc);
            setIsPaid(material, doc);
            setTargetGroups(material, doc);
            setPicture(material, doc);
            removeDuplicateTaxons(material);
            return material;
        } catch (RuntimeException e) {
            logFail(e);
            throw new ParseException(e);
        }

    }

    protected void setContributorsData(Material material, Document doc) {
        try {
            setAuthors(doc, material);
            setPublishersData(doc, material);
        } catch (Exception e) {
            logger.debug("Error while setting authors or publishers.", e.getMessage());
        }
    }

    private void removeDuplicateTaxons(Material material) {
        List<Taxon> taxons = material.getTaxons();
        List<Taxon> uniqueTaxons = new ArrayList<>(taxons);

        for (int i = 0; i < taxons.size(); i++) {
            Taxon first = taxons.get(i);

            for (int j = 0; j < taxons.size(); j++) {
                Taxon second = taxons.get(j);

                if (second.containsTaxon(first) && j != i) {
                    uniqueTaxons.remove(first);
                } else if (first.containsTaxon(second) && j != i) {
                    uniqueTaxons.remove(second);
                }
            }
        }
        material.setTaxons(uniqueTaxons);
    }

    private void setIdentifier(Material material, Document doc) {
        Element header = (Element) doc.getElementsByTagName("header").item(0);
        Element identifier = (Element) header.getElementsByTagName("identifier").item(0);
        material.setRepositoryIdentifier(identifier.getTextContent().trim());
    }

    protected void setAuthorFromVCard(List<Author> authors, String data) {
        if (data.length() > 0) {
            VCard vcard = Ezvcard.parse(data).first();
            String name = vcard.getStructuredName().getGiven();
            String surname = vcard.getStructuredName().getFamily();

            if (name != null && surname != null) {
                Author author = authorService.getAuthorByFullName(name, surname);
                if (author == null) {
                    authors.add(authorService.createAuthor(name, surname));
                } else if (!authors.contains(author)) {
                    authors.add(author);
                }
            }
        }
    }

    private void setPublisherFromVCard(List<Publisher> publishers, String data) {
        if (StringUtils.isNotEmpty(data)) {
            VCard vcard = Ezvcard.parse(data).first();

            if (CollectionUtils.isNotEmpty(vcard.getUrls())) {
                String name = vcard.getFormattedName().getValue();
                String website = vcard.getUrls().get(0).getValue();

                if (name != null && website != null) {
                    Publisher publisher = publisherService.getPublisherByName(name);
                    if (publisher == null) {
                        publishers.add(publisherService.createPublisher(name, website));
                    } else if (!publishers.contains(publisher)) {
                        publishers.add(publisher);
                    }
                }
            }
        }
    }

    protected List<LanguageString> getLanguageStrings(Node node, LanguageService languageService) {
        List<LanguageString> languageStrings = new ArrayList<>();
        NodeList nodeList = node.getChildNodes();

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node currentNode = nodeList.item(i);

            String text = currentNode.getTextContent().trim();

            if (StringUtils.isNotEmpty(text)) {
                languageStrings.add(new LanguageString(getLanguageCode(languageService, currentNode), text));
            }
        }

        return languageStrings;
    }

    private Language getLanguageCode(LanguageService languageService, Node currentNode) {
        if (currentNode.hasAttributes()) {
            String languageCode = currentNode.getAttributes().item(0).getTextContent().trim();
            String[] tokens = languageCode.split("-");

            Language language = languageService.getLanguage(tokens[0]);
            if (language != null) {
                return language;
            } else {
                String message = "No such language for '%s'. LanguageString will have no Language";
                logger.warn(String.format(message, languageCode));
            }
        }
        return null;
    }

    protected String getVCardWithNewLines(CharacterData characterData) {
        return characterData.getData().trim().trim().replaceAll("\\n\\s*(?=(\\s*))", "\r\n");
    }

    protected List<Tag> getTagsFromKeywords(NodeList keywords, TagService tagService) {
        List<String> strings = IntStream.range(0, keywords.getLength()).mapToObj(i -> keywords.item(i).getTextContent().trim().toLowerCase()).distinct().collect(Collectors.toList());
        return strings.stream().map(s -> mapTag(tagService, s)).distinct().collect(Collectors.toList());
    }

    private Tag mapTag(TagService tagService, String s) {
        Tag tag = tagService.getTagByName(s);
        return tag != null ? tag : new Tag(s);
    }

    protected List<ResourceType> getResourceTypes(Document doc, String path) {
        NodeList nl = getNodeList(doc, path);
        List<String> results = IntStream.range(0, nl.getLength()).mapToObj(i -> getElementValue(nl.item(i))).distinct().collect(Collectors.toList());
        return resourceTypeService.getResourceTypeByName(results);
    }

    private List<PeerReview> getPeerReviews(Document doc, String path) {
        NodeList nl = getNodeList(doc, path);
        List<String> results = IntStream.range(0, nl.getLength()).mapToObj(i -> getElementValue(nl.item(i))).distinct().collect(Collectors.toList());
        return peerReviewService.getPeerReviewByURL(results);
    }

    private void setEducationalContexts(Document doc, Set<Taxon> taxons, String path, Material material) {
        NodeList nl = getNodeList(doc, path);

        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            String context = getElementValue(node);

            EducationalContext educationalContext = (EducationalContext) getTaxon(context, EducationalContext.class);
            setIsSpecialEducation(material, context);

            if (educationalContext != null) {
                taxons.add(educationalContext);
            }
        }
    }

    protected String getElementValue(Node node) {
        return node.getTextContent().trim().toUpperCase();
    }

    private void setTaxon(Material material, Document doc) {
        Set<Taxon> taxons = new HashSet<>();
        Taxon parent = null;

        try {
            for (Node taxonPath : getTaxonPathNodes(doc)) {
                parent = null;
                parent = setEducationalContext(taxonPath);
                parent = setDomain(taxonPath, parent);

                parent = setSubject(taxonPath, parent);
                parent = setSpecialization(taxonPath, parent);
                parent = setModule(taxonPath, parent);

                parent = setTopic(taxonPath, parent);
                parent = setSubTopic(taxonPath, parent);

                taxons.add(parent);
            }
        } catch (Exception e) {
            taxons.add(parent);
        }

        //Set contexts that are specified separately, not inside the taxon
        setEducationalContexts(doc, taxons, getPathToContext(), material);

        taxons.removeAll(Collections.singleton(null));
        material.setTaxons(new ArrayList<>(taxons));
    }

    private void setIsSpecialEducation(Material material, String context) {
        if (context.equals("SPECIALEDUCATION")) {
            material.setSpecialEducation(true);
        }
    }

    private void setLearningResourceType(Material material, Document doc) {
        try {
            material.setResourceTypes(getResourceTypes(doc, getPathToResourceType()));
        } catch (Exception ignored) {
        }
    }

    private void setPeerReview(Material material, Document doc) {
        try {
            material.setPeerReviews(getPeerReviews(doc, getPathToPeerReview()));
        } catch (Exception ignored) {
        }
    }

    private void setSource(Material material, Document doc) throws ParseException {
        try {
            material.setSource(getSource(doc));
        } catch (Exception e) {
            throw new ParseException("Error parsing document source.");
        }

    }

    private String getSource(Document doc) throws ParseException, URISyntaxException {
        NodeList nodeList = getNodeList(doc, getPathToLocation());
        if (nodeList.getLength() != 1) {
            String message = "Material has more or less than one source, can't be mapped.";
            logger.error(message);
            throw new ParseException(message);
        }

        String source = nodeList.item(0).getTextContent().trim();

        URI uri = new URI(source);
        if (uri.getScheme() == null) {
            source = "http://" + source;
        }

        UrlValidator urlValidator = new UrlValidator(SCHEMES);
        if (!urlValidator.isValid(source)) {
            String message = "Error parsing document. Invalid URL %s";
            logger.error(String.format(message, source));
            throw new ParseException(String.format(message, source));
        }

        return source;
    }

    protected void setTargetGroups(Material material, Document doc) {
        Set<TargetGroup> targetGroups = new HashSet<>();
        NodeList ageRanges = getNodeList(doc, getPathToTargetGroups());

        for (int i = 0; i < ageRanges.getLength(); i++) {
            String ageRange = ageRanges.item(i).getTextContent().trim();
            String[] ranges = ageRange.split("-");

            if (ranges.length == 2) {
                int from = Integer.parseInt(ranges[0].trim());
                int to = Integer.parseInt(ranges[1].trim());
                targetGroups.addAll(targetGroupService.getTargetGroupsByAge(from, to));
            }
        }
        material.setTargetGroups(new ArrayList<>(targetGroups));
    }

    protected void setAuthors(Document doc, Material material) throws ParseException {
        List<Author> authors = new ArrayList<>();
        NodeList nodeList = getNodeList(doc, getPathToContribute());

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node contributorNode = nodeList.item(i);
            String role = getRoleString(contributorNode);

            if (AUTHOR.equals(role)) {
                String vCard = getVCard(contributorNode);
                setAuthorFromVCard(authors, vCard);
            }
        }

        material.setAuthors(authors);
    }

    private void setPublishersData(Document doc, Material material) {
        List<Publisher> publishers = new ArrayList<>();
        IssueDate issueDate = null;
        NodeList nodeList = getNodeList(doc, getPathToContribute());

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node contributorNode = nodeList.item(i);
            String role = getRoleString(contributorNode);

            if (PUBLISHER.equals(role)) {
                String vCard = getVCard(contributorNode);
                setPublisherFromVCard(publishers, vCard);

                Node issueDateNode = getNode(contributorNode, "./*[local-name()='date']/*[local-name()='dateTime']");
                if (issueDateNode != null) {
                    DateTime dateTime = new DateTime(issueDateNode.getTextContent().trim());
                    issueDate = new IssueDate();

                    issueDate.setDay((short) dateTime.getDayOfMonth());
                    issueDate.setMonth((short) dateTime.getMonthOfYear());
                    issueDate.setYear(dateTime.getYear());
                }
            }
        }

        material.setPublishers(publishers);
        material.setIssueDate(issueDate);
    }

    private String getRoleString(Node contributorNode) {
        try {
            Node roleNode = getNode(contributorNode, "./*[local-name()='role']/*[local-name()='value']");
            return roleNode.getTextContent().trim().toUpperCase();
        } catch (Exception ignored) {
            return null;
        }
    }

    private String getVCard(Node contributorNode) {
        Node node = getNode(contributorNode, "./*[local-name()='entity']");

        if (node != null) {
            NodeList authorNodes = node.getChildNodes();

            for (int j = 0; j < authorNodes.getLength(); j++) {
                if (!authorNodes.item(j).getTextContent().trim().isEmpty()) {
                    CharacterData characterData = (CharacterData) authorNodes.item(j);
                    return getVCardWithNewLines(characterData);
                }
            }
        }

        return "";
    }

    protected NodeList getNodeList(Node node, String path) {
        try {
            XPathExpression expr = xpath.compile(path);
            return (NodeList) expr.evaluate(node, XPathConstants.NODESET);
        } catch (XPathExpressionException ignored) {
            return null;
        }

    }

    protected Node getNode(Node node, String path) {
        try {
            XPathExpression expr = xpath.compile(path);
            return (Node) expr.evaluate(node, XPathConstants.NODE);
        } catch (XPathExpressionException ignored) {
            return null;
        }
    }

    private List<Node> getTaxonPathNodes(Document doc) {
            List<Node> nodes = new ArrayList<>();
            NodeList classifications = getNodeList(doc, getPathToClassification());

            for (int i = 0; i < classifications.getLength(); i++) {
            NodeList nl = getNodeList(classifications.item(i), TAXON_PATH);

            if (notEmpty(nl)) {
                    for (int j = 0; j < nl.getLength(); j++) {
                        nodes.add(nl.item(j));
                    }
                }
            }
            return nodes;

    }

    protected Taxon setEducationalContext(Node taxonPath) {
        for (Map.Entry<String, String> tag : taxonMap.entrySet()) {
            Node node = getNode(taxonPath, "./*[local-name()='" + tag.getKey() + "']");
            if (node != null) {
                return getTaxon(tag.getValue(), EducationalContext.class);
            }
        }
        return null;
    }

    protected Taxon setDomain(Node taxonPath, Taxon educationalContext) {
        for (String tag : taxonMap.keySet()) {
            Node node = getNode(taxonPath, taxonPath(tag, "domain"));

            if (node != null) {
                List<Taxon> domains = new ArrayList<>(((EducationalContext) educationalContext).getDomains());
                String systemName = getTaxon(node.getTextContent(), Domain.class).getName();

                Taxon taxon = getTaxonByName(domains, systemName);
                if (taxon != null)
                    return taxon;
            }
        }

        return educationalContext;
    }

    protected Taxon setSubject(Node taxonPath, Taxon domain) {
        for (String tag : taxonMap.keySet()) {
            Node node = getNode(taxonPath, taxonPath(tag, "subject"));

            if (node != null) {
                List<Taxon> subjects = new ArrayList<>(((Domain) domain).getSubjects());
                String systemName = getTaxon(node.getTextContent(), Subject.class).getName();
                Taxon taxon = getTaxonByName(subjects, systemName);

                if (taxon != null)
                    return taxon;
            }
        }

        return domain;
    }

    private Taxon setTopic(Node taxonPath, Taxon parent) {
        for (String tag : taxonMap.keySet()) {
            Node node = getNode(taxonPath, taxonPath(tag, "topic"));

            if (node != null) {
                List<Taxon> topics = null;
                if (parent instanceof Module && tag.equals("vocationalTaxon")) {
                    topics = new ArrayList<>(((Module) parent).getTopics());
                } else if (parent instanceof Domain && tag.equals("preschoolTaxon")) {
                    topics = new ArrayList<>(((Domain) parent).getTopics());
                } else if (parent instanceof Subject) {
                    topics = new ArrayList<>(((Subject) parent).getTopics());
                }

                if (topics != null) {
                    String systemName = getTaxon(node.getTextContent(), Topic.class).getName();
                    Taxon taxon = getTaxonByName(topics, systemName);
                    if (taxon != null)
                        return taxon;
                }
            }
        }

        return parent;
    }

    private Taxon setSpecialization(Node taxonPath, Taxon parent) {
        for (String tag : taxonMap.keySet()) {
            Node node = getNode(taxonPath, taxonPath(tag, "specialization"));

            if (node != null) {
                List<Taxon> specializations = new ArrayList<>(((Domain) parent).getSpecializations());
                String systemName = getTaxon(node.getTextContent(), Specialization.class).getName();
                Taxon taxon = getTaxonByName(specializations, systemName);
                if (taxon != null)
                    return taxon;
            }
        }

        return parent;
    }

    private Taxon setModule(Node taxonPath, Taxon parent) {
        for (String tag : taxonMap.keySet()) {
            Node node = getNode(taxonPath, taxonPath(tag, "module"));

            if (node != null) {
                List<Taxon> modules = new ArrayList<>(((Specialization) parent).getModules());

                String systemName = getTaxon(node.getTextContent(), Module.class).getName();
                Taxon taxon = getTaxonByName(modules, systemName);
                if (taxon != null)
                    return taxon;
            }
        }
        return parent;
    }

    private Taxon setSubTopic(Node taxonPath, Taxon parent) {
        for (String tag : taxonMap.keySet()) {
            Node node = getNode(taxonPath, "./*[local-name()='" + tag + "']/*[local-name()='subtopic']");
            if (node != null) {
                List<Taxon> subtopics = new ArrayList<>(((Topic) parent).getSubtopics());

                String systemName = getTaxon(node.getTextContent(), Subtopic.class).getName();
                Taxon taxon = getTaxonByName(subtopics, systemName);
                if (taxon != null)
                    return taxon;
            }
        }
        return parent;
    }

    private Taxon getTaxonByName(List<Taxon> topics, String systemName) {
        return topics.stream().filter(taxon -> taxon.getName().equals(systemName)).findAny().orElse(null);
    }

    protected abstract void setTags(Material material, Document doc);

    protected abstract void setDescriptions(Material material, Document doc);

    protected abstract void setLanguage(Material material, Document doc);

    protected abstract void setTitles(Material material, Document doc) throws ParseException;

    protected abstract String getPathToContext();

    protected abstract String getPathToResourceType();

    protected abstract String getPathToPeerReview();

    protected abstract String getPathToLocation();

    protected abstract String getPathToContribute();

    protected abstract void setIsPaid(Material material, Document doc);

    protected abstract String getPathToTargetGroups();

    protected abstract String getPathToCurriculumLiterature();

    protected abstract void setPicture(Material material, Document doc);

    protected abstract void setCrossCurricularThemes(Material material, Document doc);

    protected abstract void setKeyCompetences(Material material, Document doc);

    protected abstract String getPathToClassification();

    protected abstract Taxon getTaxon(String context, Class level);

    private String taxonPath(String tag, String domain) {
        return "./*[local-name()='" + tag + "']/*[local-name()='" + domain + "']";
    }

    private void logFail(RuntimeException e) {
        logger.error("Unexpected error while parsing document. Document may not"
                + " match mapping or XML structure - " + e.getMessage(), e);
    }
}
