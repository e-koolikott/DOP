package ee.hm.dop.service.content;

import ee.hm.dop.dao.OriginalPictureDao;
import ee.hm.dop.dao.ThumbnailDao;
import ee.hm.dop.model.OriginalPicture;
import ee.hm.dop.model.Picture;
import ee.hm.dop.model.Thumbnail;
import ee.hm.dop.service.files.PictureSaver;
import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

import static ee.hm.dop.utils.DOPFileUtils.getFileAsStream;
import static ee.hm.dop.utils.DOPFileUtils.read;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

@RunWith(EasyMockRunner.class)
public class PictureServiceTest {

    private static final String IMAGE1_SHA1_HASH = "ce7870d17769da42406687d2ad72713ea3b4a6bd";

    @TestSubject
    private PictureSaver pictureSaver = new PictureSaver();
    @Mock
    private OriginalPictureDao originalPictureDao;
    @Mock
    private ThumbnailDao thumbnailDao;

    @Test
    public void creating_new_image_creates_new_image() {
        byte[] image1 = read(getFileAsStream("images/image1.jpg"), 1);

        Picture picture = new OriginalPicture();
        picture.setData(image1);

        Thumbnail thumbnail = new Thumbnail();
        thumbnail.setData(image1);

        expect(originalPictureDao.findByNameAny(IMAGE1_SHA1_HASH)).andReturn(null);
        expect(thumbnailDao.createOrUpdate(EasyMock.anyObject(Thumbnail.class))).andReturn(new Thumbnail()).times(4);
        expect(originalPictureDao.createOrUpdate((OriginalPicture) picture)).andReturn((OriginalPicture) picture);

        replay(thumbnailDao);
        replay(originalPictureDao);

        Picture createdPicture = pictureSaver.create(picture);

        verify(originalPictureDao);

        assertNotNull(createdPicture.getName());
        assertArrayEquals(picture.getData(), createdPicture.getData());
    }


    @Test
    public void createWhenHashMatches() {
        OriginalPicture existingPicture = new OriginalPicture();
        existingPicture.setId(1);

        byte[] image1 = read(getFileAsStream("images/image1.jpg"), 1);
        existingPicture.setData(image1);

        Picture picture = new OriginalPicture();
        picture.setData(image1);

        expect(originalPictureDao.findByNameAny(IMAGE1_SHA1_HASH)).andReturn(existingPicture).anyTimes();
        expect(originalPictureDao.createOrUpdate((OriginalPicture) picture)).andReturn((OriginalPicture) picture);

        replay(originalPictureDao);

        Picture createdPicture = pictureSaver.create(picture);

        verify(originalPictureDao);

        assertNotEquals(existingPicture, createdPicture);
        assertNotEquals(existingPicture.getId(), createdPicture.getId());
    }
}
