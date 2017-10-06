package ee.hm.dop.rest;

import ee.hm.dop.model.OriginalPicture;
import ee.hm.dop.model.Picture;
import ee.hm.dop.model.enums.RoleString;
import ee.hm.dop.model.Thumbnail;
import ee.hm.dop.service.content.PictureService;
import org.apache.commons.configuration.Configuration;
import org.apache.http.HttpHeaders;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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

    @GET
    @Path("/{name}")
    @Produces("image/png")
    public Response getPictureDataByName(@PathParam("name") String pictureName) {
        Picture picture = pictureService.getByName(pictureName);
        return getPictureResponseWithCache(picture);
    }

    @GET
    @Path("thumbnail/sm/{name}")
    @Produces("image/png")
    public Response getSMThumbnailDataByName(@PathParam("name") String pictureName) {
        Thumbnail thumbnail = pictureService.getSMThumbnailByName(pictureName);
        return getPictureResponseWithCache(thumbnail);
    }

    @GET
    @Path("thumbnail/sm_xs_xl/{name}")
    @Produces("image/png")
    public Response getSMLargeThumbnailDataByName(@PathParam("name") String pictureName) {
        Thumbnail thumbnail = pictureService.getSMLargeThumbnailByName(pictureName);
        return getPictureResponseWithCache(thumbnail);
    }

    @GET
    @Path("thumbnail/lg/{name}")
    @Produces("image/png")
    public Response getLGThumbnailDataByName(@PathParam("name") String pictureName) {
        Thumbnail thumbnail = pictureService.getLGThumbnailByName(pictureName);
        return getPictureResponseWithCache(thumbnail);
    }

    @GET
    @Path("thumbnail/lg_xs/{name}")
    @Produces("image/png")
    public Response getLGLargeThumbnailDataByName(@PathParam("name") String pictureName) {
        Thumbnail thumbnail = pictureService.getLGLargeThumbnailByName(pictureName);
        return getPictureResponseWithCache(thumbnail);
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
        return pictureService.create(picture);
    }

    @PUT
    @Path("/fromUrl")
    @Produces(MediaType.APPLICATION_JSON)
    public Picture uploadPictureFromURL(String url) {
        return pictureService.createFromURL(url);
    }

    @GET
    @Path("/maxSize")
    @Produces(MediaType.APPLICATION_JSON)
    public int getMaxSize() {
        return configuration.getInt(MAX_FILE_SIZE);
    }
}
