package ee.hm.dop.service.statistics;

import ee.hm.dop.dao.LanguageDao;
import ee.hm.dop.dao.UserDao;
import ee.hm.dop.model.User;
import ee.hm.dop.service.reviewmanagement.dto.StatisticsFilterDto;
import ee.hm.dop.service.reviewmanagement.newdto.EducationalContextRow;
import ee.hm.dop.service.reviewmanagement.newdto.NewStatisticsResult;
import ee.hm.dop.service.reviewmanagement.newdto.NewStatisticsRow;
import ee.hm.dop.utils.DopConstants;
import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static ee.hm.dop.service.statistics.StatisticsUtil.EMPTY_ROW;
import static ee.hm.dop.service.statistics.StatisticsUtil.NO_USER_FOUND;

@Service
@Transactional
public class NewStatisticsExcelExporter {

    private static final Logger logger = LoggerFactory.getLogger(NewStatisticsExcelExporter.class);

    @Inject
    private LanguageDao languageDao;
    @Inject
    private UserDao userDao;
    @Inject
    private CommonStatisticsExporter commonStatisticsExporter;

    public void generateXlsx(String filename, NewStatisticsResult statistics) {
        generate(filename, statistics, new XSSFWorkbook());
    }

    public void generateXls(String filename, NewStatisticsResult statistics) {
        generate(filename, statistics, new HSSFWorkbook());
    }

    private void generate(String filename, NewStatisticsResult statistics, Workbook book) {
        Long estId = languageDao.findByCode(DopConstants.LANGUAGE_ET).getId();
        Workbook workbook = createWorkBook(statistics, estId, book);

        try (FileOutputStream outputStream = new FileOutputStream(filename)) {
            workbook.write(outputStream);
        } catch (IOException e) {
            logger.error(statistics.getFilter().getFormat().name() + " file generation failed");
        }
    }

    private Workbook createWorkBook(NewStatisticsResult statistics, Long estId, Workbook book) {
        Sheet sheet = book.createSheet("Statistika aruanne");
        NewStatisticsResult sortedStatistics = commonStatisticsExporter.translateAndSort(statistics, estId);
        if (sortedStatistics.getFilter().isUserSearch()) {
            writeUserExcel(sortedStatistics, estId, sheet);
        } else {
            writeTaxonExcel(sortedStatistics, estId, sheet);
        }
        return book;
    }

    private void writeTaxonExcel(NewStatisticsResult statistics, Long estId, Sheet sheet) {
        int rowNum = 0;
        int xlsColNum = 0;
        Row headersRow = sheet.createRow(rowNum++);
        for (String heading : commonStatisticsExporter.taxonHeader(statistics, estId)) {
            Cell cell = headersRow.createCell(xlsColNum++);
            cell.setCellValue(heading);
        }
        xlsColNum = 0;
        headersRow = sheet.createRow(rowNum++);
        for (String heading : StatisticsUtil.TAXON_HEADERS) {
            Cell cell = headersRow.createCell(xlsColNum++);
            cell.setCellValue(heading);
        }

        List<EducationalContextRow> ecRows = statistics.getRows();
        for (EducationalContextRow s : ecRows) {
            List<NewStatisticsRow> rows = s.getRows();
            for (NewStatisticsRow row : rows) {
                if (row.isNoUsersFound()) {
                    rowNum = writeNoUserFoundRow(sheet, rowNum, row, null, estId);
                }
                if (row.isDomainUsed()) {
                    rowNum = writeTaxonRow(sheet, rowNum, row, null, estId);
                }
                if (CollectionUtils.isNotEmpty(row.getSubjects())) {
                    for (NewStatisticsRow childRow : row.getSubjects()) {
                        rowNum = writeTaxonRow(sheet, rowNum, childRow, null, estId);
                    }
                }
            }
        }
        writeTaxonRow(sheet, rowNum, statistics.getSum(), "Kokku", estId);
    }

    private void writeUserExcel(NewStatisticsResult statistics, Long estId, Sheet sheet) {
        StatisticsFilterDto filter = statistics.getFilter();
        int rowNum = 0;
        int xlsColNum = 0;
        Row headersRow = sheet.createRow(rowNum++);
        User userDto = filter.getUsers().get(0);
        User user = userDao.findById(userDto.getId());
        for (String heading : StatisticsUtil.userHeader(filter.getFrom(), filter.getTo(), user)) {
            Cell cell = headersRow.createCell(xlsColNum++);
            cell.setCellValue(heading);
        }
        xlsColNum = 0;
        headersRow = sheet.createRow(rowNum++);
        for (String heading : StatisticsUtil.USER_HEADERS) {
            Cell cell = headersRow.createCell(xlsColNum++);
            cell.setCellValue(heading);
        }

        List<EducationalContextRow> ecRows = statistics.getRows();
        for (EducationalContextRow s : ecRows) {
            List<NewStatisticsRow> rows = s.getRows();
            for (NewStatisticsRow row : rows) {
                if (row.isDomainUsed()) {
                    rowNum = writeUserRow(sheet, rowNum, row, null, estId);
                }
                if (CollectionUtils.isNotEmpty(row.getSubjects())) {
                    for (NewStatisticsRow childRow : row.getSubjects()) {
                        rowNum = writeUserRow(sheet, rowNum, childRow, null, estId);
                    }
                }
            }
        }
        writeUserRow(sheet, rowNum, statistics.getSum(), "Kokku", estId);
    }

    private int writeUserRow(Sheet sheet, int rowNum, NewStatisticsRow s, String sumRowText, Long estId) {
        int xlsColNum = 0;
        Row row = sheet.createRow(rowNum++);
        Cell cell;
        cell = row.createCell(xlsColNum++);
        cell.setCellValue(sumRowText != null ? "" : commonStatisticsExporter.translationOrName(estId, s.getEducationalContext()));
        cell = row.createCell(xlsColNum++);
        cell.setCellValue(sumRowText != null ? "" : commonStatisticsExporter.translationOrName(estId, s.getDomain()));
        cell = row.createCell(xlsColNum++);
        cell.setCellValue(sumRowText != null ? sumRowText : s.getSubject() != null ? commonStatisticsExporter.translationOrName(estId, s.getSubject()) : "");
        cell = row.createCell(xlsColNum++);
        cell.setCellValue(s.getReviewedLOCount());
        cell = row.createCell(xlsColNum++);
        cell.setCellValue(s.getApprovedReportedLOCount());
        cell = row.createCell(xlsColNum++);
        cell.setCellValue(s.getDeletedReportedLOCount());
        cell = row.createCell(xlsColNum++);
        cell.setCellValue(s.getAcceptedChangedLOCount());
        cell = row.createCell(xlsColNum++);
        cell.setCellValue(s.getRejectedChangedLOCount());
        cell = row.createCell(xlsColNum++);
        cell.setCellValue(s.getPortfolioCount());
        cell = row.createCell(xlsColNum++);
        cell.setCellValue(s.getPublicPortfolioCount());
        cell = row.createCell(xlsColNum++);
        cell.setCellValue(s.getMaterialCount());
        return rowNum;
    }

    private int writeNoUserFoundRow(Sheet sheet, int rowNum, NewStatisticsRow s, String sumRowText, Long estId) {
        int xlsColNum = 0;
        Row row = sheet.createRow(rowNum++);
        Cell cell;
        cell = row.createCell(xlsColNum++);
        cell.setCellValue(sumRowText != null ? "" : commonStatisticsExporter.translationOrName(estId, s.getEducationalContext()));
        cell = row.createCell(xlsColNum++);
        cell.setCellValue(sumRowText != null ? "" : commonStatisticsExporter.translationOrName(estId, s.getDomain()));
        cell = row.createCell(xlsColNum++);
        cell.setCellValue(sumRowText != null ? sumRowText : s.getSubject() != null ? commonStatisticsExporter.translationOrName(estId, s.getSubject()) : "");
        cell = row.createCell(xlsColNum++);
        cell.setCellValue(NO_USER_FOUND);
        cell = row.createCell(xlsColNum++);
        cell.setCellValue(EMPTY_ROW);
        cell = row.createCell(xlsColNum++);
        cell.setCellValue(EMPTY_ROW);
        cell = row.createCell(xlsColNum++);
        cell.setCellValue(EMPTY_ROW);
        cell = row.createCell(xlsColNum++);
        cell.setCellValue(EMPTY_ROW);
        cell = row.createCell(xlsColNum++);
        cell.setCellValue(EMPTY_ROW);
        cell = row.createCell(xlsColNum++);
        cell.setCellValue(EMPTY_ROW);
        cell = row.createCell(xlsColNum++);
        cell.setCellValue(EMPTY_ROW);
        cell = row.createCell(xlsColNum++);
        cell.setCellValue(EMPTY_ROW);
        return rowNum;
    }

    private int writeTaxonRow(Sheet sheet, int rowNum, NewStatisticsRow s, String sumRowText, Long estId) {
        int xlsColNum = 0;
        Row row = sheet.createRow(rowNum++);
        Cell cell;
        cell = row.createCell(xlsColNum++);
        cell.setCellValue(sumRowText != null ? "" : commonStatisticsExporter.translationOrName(estId, s.getEducationalContext()));
        cell = row.createCell(xlsColNum++);
        cell.setCellValue(sumRowText != null ? "" : commonStatisticsExporter.translationOrName(estId, s.getDomain()));
        cell = row.createCell(xlsColNum++);
        cell.setCellValue(sumRowText != null ? "" : s.getSubject() != null ? commonStatisticsExporter.translationOrName(estId, s.getSubject()) : "");
        cell = row.createCell(xlsColNum++);
        cell.setCellValue(sumRowText != null ? sumRowText : s.getUser().getFullName());
        cell = row.createCell(xlsColNum++);
        cell.setCellValue(s.getReviewedLOCount());
        cell = row.createCell(xlsColNum++);
        cell.setCellValue(s.getApprovedReportedLOCount());
        cell = row.createCell(xlsColNum++);
        cell.setCellValue(s.getDeletedReportedLOCount());
        cell = row.createCell(xlsColNum++);
        cell.setCellValue(s.getAcceptedChangedLOCount());
        cell = row.createCell(xlsColNum++);
        cell.setCellValue(s.getRejectedChangedLOCount());
        cell = row.createCell(xlsColNum++);
        cell.setCellValue(s.getPortfolioCount());
        cell = row.createCell(xlsColNum++);
        cell.setCellValue(s.getPublicPortfolioCount());
        cell = row.createCell(xlsColNum++);
        cell.setCellValue(s.getMaterialCount());
        return rowNum;
    }
}
