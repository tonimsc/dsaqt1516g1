package edu.upc.eetac.dsa.videostore;


import edu.upc.eetac.dsa.videostore.DAO.*;
import edu.upc.eetac.dsa.videostore.entity.Buys;
import edu.upc.eetac.dsa.videostore.entity.Movie;
import edu.upc.eetac.dsa.videostore.entity.MoviesCollection;
import edu.upc.eetac.dsa.videostore.entity.Resources;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;

@Path("movie")
public class MovieResource {
    @Context
    private SecurityContext securityContext;

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(VideostoreMediaType.VIDEOSTORE_MOVIE)
    public Response createMovie(@FormParam("titulo") String titulo, @FormParam("genero") String genero,
    @FormParam("ano") int año, @FormParam("director") String director, @FormParam("descripcion") String descripcion,
    @FormParam("votos") int votos, @FormParam("numdesc") int numerodescargaspermitidas, @FormParam("tempmax") int tiempomaximovisualizacion,
    @FormParam("palquiler") int precioalquiler, @FormParam("pcompra") int preciocompra, @FormParam("portada") String recursoportada,
                                @Context UriInfo uriInfo)
            throws URISyntaxException {

        boolean admin = securityContext.isUserInRole("admin");
        if(!admin)
            throw new ForbiddenException("operation not allowed");

         if (titulo == null || genero == null || año == 0 || director == null || descripcion == null ||
         numerodescargaspermitidas == 0 || tiempomaximovisualizacion == 0 || precioalquiler == 0 || preciocompra == 0)
         throw new BadRequestException("all parameters are mandatory");

        MovieDAO movieDAO = new MovieDAOImpl();
        Movie movie = null;
        try {
            movie = movieDAO.createMovie(titulo, genero, año, director, descripcion, votos, numerodescargaspermitidas,
                    tiempomaximovisualizacion, precioalquiler, preciocompra, recursoportada);
        }
        catch (SQLException e) {
            throw new InternalServerErrorException();
        }
        catch (MovieAlreadyExistsException e) {
        throw new WebApplicationException("movieid already exists", Response.Status.CONFLICT);
         }
        URI uri = new URI(uriInfo.getAbsolutePath().toString() + "/" + movie.getId());
        return Response.created(uri).type(VideostoreMediaType.VIDEOSTORE_MOVIE).entity(movie).build();
    }
    @Path("/search")
    @GET
    @Produces(VideostoreMediaType.VIDEOSTORE_MOVIES_COLLECTION)
    public MoviesCollection getMoviesby(@QueryParam("tipo") String tipo, @DefaultValue("null") @QueryParam("valor") String valor,
                                        @Context UriInfo uriInfo) {
        if(tipo == null)
            throw new BadRequestException("'tipo' parameters are mandatory");
        MoviesCollection mCollection = null;
        MovieDAO mDAO = new MovieDAOImpl();
        switch (tipo) {
            case "ano":
                try {
                    int año = Integer.parseInt(valor);
                    mCollection = mDAO.getMoviesbyYEAR(año);
                }
                catch(SQLException e)
                {
                    throw new InternalServerErrorException();
                }
                break;
            case "titulo":
                try {
                    mCollection = mDAO.getMoviesbyTITLE(valor);
                }
                catch(SQLException e)
                {
                    throw new InternalServerErrorException();
                }
                break;
            case "director":
                try {
                    mCollection = mDAO.getMoviesbyDIRECTOR(valor);
                }
                catch(SQLException e)
                {
                    throw new InternalServerErrorException();
                }
                break;
            case "last":
                try {
                    mCollection = mDAO.getMoviesbyLASTADDED();
                }
                catch(SQLException e)
                {
                    throw new InternalServerErrorException();
                }
                break;
            case "valoradas":
                try {
                    mCollection = mDAO.getMoviesbyVOTES();
                }
                catch(SQLException e)
                {
                    throw new InternalServerErrorException();
                }
                break;
            case "destacadas":
                try {
                    mCollection = mDAO.getMoviesbyDEST();
                }
                catch(SQLException e)
                {
                    throw new InternalServerErrorException();
                }
                break;
        }

        return mCollection;
    }

    @Path("/{id}")
    @GET
    @Produces(VideostoreMediaType.VIDEOSTORE_MOVIE)
    public Movie getMovie(@PathParam("id") String id, @Context Request request) {
        Movie movie = null;
        MovieDAO movieDAO = new MovieDAOImpl();
        try {
            movie = movieDAO.getMoviebyID(id);
            if (movie == null)
                throw new NotFoundException("Movie with id = " + id + " doesn't exist");

        } catch (SQLException e) {
            throw new InternalServerErrorException();
        }
        return movie;
    }

    @Path("/compra/{idusuario}/{idmovie}")
    @GET
    @Produces(VideostoreMediaType.VIDEOSTORE_MOVIE)
    public Resources getMovieResourceBuy(@PathParam("idusuario") String iduser, @PathParam("idmovie") String idmovie, @Context Request request) {
        OperationDAO operationDAO = new OperationDAOImpl();
        ResourcesDAO resourcesDAO = new ResourcesDAOImpl();
        Resources resources;
        Buys buys = new Buys();

        String userid = securityContext.getUserPrincipal().getName();
        if(!userid.equals(iduser))
            throw new ForbiddenException("operation not allowed");

        try {
            boolean comprado = operationDAO.alreadyBuy(iduser,idmovie);
            if (comprado)
            {
                resources = resourcesDAO.getResource(idmovie);
                buys = operationDAO.getBuyByIDmovieandUser(iduser,idmovie);
                int restante = buys.getDowmloadedtimes() - 1;
                operationDAO.updateBuy(buys.getUserid(), buys.getMovieid(), restante);
            }
            else
                throw new ForbiddenException("operation not allowed");
        } catch (SQLException e) {
            throw new InternalServerErrorException();
        }
        return resources;
    }
    @Path("/alquiler/{idusuario}/{idmovie}")
    @GET
    @Produces(VideostoreMediaType.VIDEOSTORE_MOVIE)
    public Resources getMovieResourceRent(@PathParam("idusuario") String iduser, @PathParam("idmovie") String idmovie, @Context Request request) {
        OperationDAO operationDAO = new OperationDAOImpl();
        ResourcesDAO resourcesDAO = new ResourcesDAOImpl();
        Resources resources;

        String userid = securityContext.getUserPrincipal().getName();
        if(!userid.equals(iduser))
            throw new ForbiddenException("operation not allowed");

        try {
            boolean alquilado = operationDAO.alreadyRent(iduser,idmovie);
            if (alquilado)
            {
                resources = resourcesDAO.getResource(idmovie);
            }
            else
                throw new ForbiddenException("operation not allowed");
        } catch (SQLException e) {
            throw new InternalServerErrorException();
        }
        return resources;
    }

    @Path("/{id}")
    @PUT
    @Consumes(VideostoreMediaType.VIDEOSTORE_MOVIE)
    @Produces(VideostoreMediaType.VIDEOSTORE_MOVIE)
    public Movie updateMovie(@PathParam("id") String id, Movie movies) {
        if (movies == null)
            throw new BadRequestException("entity is null");
        if (!id.equals(movies.getId()))
            throw new BadRequestException("path parameter id and entity parameter id doesn't match");

        boolean admin = securityContext.isUserInRole("admin");
        if(!admin)
            throw new ForbiddenException("operation not allowed");
        MovieDAO movieDAO = new MovieDAOImpl();
        Movie movie = null;
        try {
            movie = movieDAO.updateMovie(id, movies.getTitle(), movies.getGenre(), movies.getYear(), movies.getDirector(),
                    movies.getDescription(), movies.getVotes(), movies.getNummaxdownloads(), movies.getMaxtimeshow(),
                    movies.getRentcost(), movies.getBuycost(), movies.getResourcecover());
            if (movie == null)
                throw new NotFoundException("Movie with id = " + id + " doesn't exist");
        } catch (SQLException e) {
            throw new InternalServerErrorException();
        }
        return movie;
    }

    @Path("/vote/{id}")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(VideostoreMediaType.VIDEOSTORE_MOVIE)
    public Movie summitVote(@FormParam("idusuario") String iduser, @PathParam("id") String idmovie) {
        if(iduser == null || idmovie == null)
            throw new BadRequestException("all parameters are mandatory");

        String userid = securityContext.getUserPrincipal().getName();
        if(!userid.equals(iduser))
            throw new ForbiddenException("operation not allowed");

        VotesDAO votesDAO = new VotesDAOImpl();
        Movie movie = null;
        try {
            if(votesDAO.sumitVote(iduser, idmovie))
            {
                MovieDAO movieDAO = new MovieDAOImpl();
                movie = movieDAO.getMoviebyID(idmovie);
                if (movie == null)
                    throw new NotFoundException("Movie with id = " + idmovie + " doesn't exist");
            }
            else
                throw new BadRequestException("Movie with id = " + idmovie + " already voted");

        } catch (SQLException e) {
            throw new InternalServerErrorException();
        }
        return movie;
    }

    @Path("/{id}")
    @DELETE
    public void deleteMovie(@PathParam("id") String id) {
        MovieDAO movieDAO = new MovieDAOImpl();
        boolean admin = securityContext.isUserInRole("admin");
        if(!admin)
            throw new ForbiddenException("operation not allowed");
        try {
            if (!movieDAO.deleteMovie(id))
                throw new NotFoundException("Movie with id = " + id + " doesn't exist");
        } catch (SQLException e) {
            throw new InternalServerErrorException();
        }
    }


}
