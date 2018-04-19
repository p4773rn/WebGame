package website;

import lobby.LobbyList;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("/lobbylist")
public class LobbyListService {
    @GET
    @Path("/num_lobbies")
    public Response getUserId() {
        return Response.ok(LobbyList.getLobbies().size()).build();
    }

    @GET
    @Path("/map")
    public Response getMap(@QueryParam("lobbyIndex") int lobbyIndex) {
        return Response.ok(LobbyList.getLobbies().get(lobbyIndex).getMapJson()).build();
    }
}
