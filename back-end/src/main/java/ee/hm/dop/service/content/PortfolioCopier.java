package ee.hm.dop.service.content;

import com.google.common.collect.Lists;
import ee.hm.dop.model.Chapter;
import ee.hm.dop.model.ChapterBlock;
import ee.hm.dop.model.Portfolio;
import ee.hm.dop.model.User;
import ee.hm.dop.service.permission.PortfolioPermission;
import ee.hm.dop.utils.ValidatorUtil;
import org.apache.commons.collections.CollectionUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

public class PortfolioCopier {

    @Inject
    private PortfolioService portfolioService;
    @Inject
    private PortfolioGetter portfolioGetter;
    @Inject
    private PortfolioConverter portfolioConverter;
    @Inject
    private PortfolioPermission portfolioPermission;

    public Portfolio copy(Portfolio portfolio, User loggedInUser) {
        Portfolio originalPortfolio = portfolioGetter.findValid(portfolio);

        if (!portfolioPermission.canView(loggedInUser, originalPortfolio)) {
            throw ValidatorUtil.permissionError();
        }

        Portfolio copy = portfolioConverter.getPortfolioWithAllowedFieldsOnCreate(originalPortfolio);
        copy.setChapters(copyChapters(originalPortfolio.getChapters()));

        return portfolioService.doCreate(copy, loggedInUser, originalPortfolio.getCreator());
    }

    private List<Chapter> copyChapters(List<Chapter> chapters) {
        if (CollectionUtils.isEmpty(chapters)) {
            return Lists.newArrayList();
        }
        return chapters.stream().map(this::copy).collect(Collectors.toList());
    }

    private Chapter copy(Chapter chapter) {
        Chapter copy = new Chapter();
        copy.setTitle(chapter.getTitle());
        copy.setText(chapter.getText());
        copy.setBlocks(chapter.getBlocks());
        copy.setContentRows(chapter.getContentRows());
        copy.setSubchapters(copyChapters(chapter.getSubchapters()));
        if (isNotEmpty(chapter.getBlocks())) {
            copy.setBlocks(chapter.getBlocks().stream().map(this::copyBlock).collect(Collectors.toCollection(ArrayList::new)));
        }
        return copy;
    }

    private ChapterBlock copyBlock(ChapterBlock from) {
        ChapterBlock to = new ChapterBlock();
        to.setNarrow(from.isNarrow());
        to.setHtmlContent(from.getHtmlContent());
        return to;
    }
}
