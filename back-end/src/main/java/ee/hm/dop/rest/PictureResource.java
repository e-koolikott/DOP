package ee.hm.dop.rest;

import ee.hm.dop.model.OriginalPicture;
import ee.hm.dop.model.Picture;
import ee.hm.dop.model.enums.RoleString;
import ee.hm.dop.model.enums.Size;
import ee.hm.dop.service.files.PictureSaver;
import ee.hm.dop.service.files.PictureService;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.net.HttpURLConnection;

import static ee.hm.dop.utils.ConfigurationProperties.MAX_FILE_SIZE;
import static ee.hm.dop.utils.DOPFileUtils.read;
import static org.apache.commons.codec.binary.Base64.decodeBase64;

@Path("picture")
public class PictureResource extends BaseResource {

    public static final String MAX_AGE_1_YEAR = "max-age=31536000";
    @Inject
    private PictureService pictureService;
    @Inject
    private Configuration configuration;
    @Inject
    private PictureSaver pictureSaver;

    @GET
    @Path("/{name}")
    @Produces("image/png")
    public Response getPictureDataByName(@PathParam("name") String pictureName) {
        return getPictureResponseWithCache(pictureService.getByName(pictureName));
    }

    @GET
    @Path("thumbnail/{size}/{name}")
    @Produces("image/png")
    public Response getSMThumbnailDataByName(@PathParam("size") String sizeString, @PathParam("name") String pictureName) {
        if (StringUtils.isBlank(sizeString)) {
            throw new UnsupportedOperationException("no size");
        }
        Size size = Size.valueOf(sizeString.toUpperCase());
        return getPictureResponseWithCache(pictureService.getThumbnailByName(pictureName, size));
    }

    private Response getPictureResponseWithCache(Picture picture) {
        if (picture != null) {
            byte[] data = picture.getData();
            return Response.ok(data).header(HttpHeaders.CACHE_CONTROL, MAX_AGE_1_YEAR).build();
        }
        return Response.status(HttpURLConnection.HTTP_NOT_FOUND).build();
    }

    @POST
    @RolesAllowed({RoleString.USER, RoleString.ADMIN, RoleString.MODERATOR})
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Picture uploadPicture(@FormDataParam("picture") InputStream fileInputStream) {
        byte[] dataBase64 = read(fileInputStream, configuration.getInt(MAX_FILE_SIZE));
        byte[] data = decodeBase64(dataBase64);

        Picture picture = new OriginalPicture();
        picture.setData(data);
        return pictureSaver.create(picture);
    }

    @PUT
    @Path("/fromUrl")
    @Produces(MediaType.APPLICATION_JSON)
    public Picture uploadPictureFromURL(String url) {
        return pictureSaver.createFromURL(url);
    }

    @GET
    @Path("/maxSize")
    @Produces(MediaType.APPLICATION_JSON)
    public int getMaxSize() {
        return configuration.getInt(MAX_FILE_SIZE);
    }
}
