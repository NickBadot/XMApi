import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchema;

public class XMApiTest {
    final String FILM = "films";
    final String CHARACTER = "people";
    // We will use the following  lists frequently, populate during setup phase
    List<Response> film_list;
    List<Response> character_list;

    @BeforeClass
    void setup() {
        System.out.println("Starting Test Suite...");
        int number_of_movies = getMovieList().jsonPath().getInt("count");
        this.film_list = buildResponseList(FILM, number_of_movies);
        System.out.println("Film List built with " + number_of_movies + " items");
        int number_of_characters = getCharacterList().jsonPath().getInt("count");
        this.character_list = buildResponseList(CHARACTER, number_of_characters);
        System.out.println("Character List built with " + number_of_characters + " items");
    }

    @Test
    void testLatestFilm() throws ParseException {
        // Despite claims otherwise, SWApi does not stock data on the latest SW trilogy
        String expected = "Revenge of the Sith";
        Response latest_film = getLatestFilm();
        assert (latest_film.jsonPath().get("title").equals(expected));
    }

    @Test
    void testTallestCharacterInLatestFilm() throws ParseException {
        String expected = "Tarfful";
        Response latest_film = getLatestFilm();
        List<String> character_uris = latest_film.jsonPath().getList("characters");
        String tallest = getTallestCharacterFromUriList(character_uris);
        assert (tallest.equals(expected));
    }

    @Test
    void testTallestCharacterInAllFilms() {
        String expected = "Yarael Poof";
        String answer = getTallestCharacterFromResponseList();
        assert (answer.equals(expected));
    }

    @Test
    void contractJsonValidationCharacter() {
        File json_schema = new File("src/test/character_schema.json");
        given().
                get("https://swapi.dev/api/people/3").
                then().
                body(matchesJsonSchema(json_schema));
    }

    public Response getRequest(String uri) {
        RestAssured.defaultParser = Parser.JSON;
        return given().headers("Content-Type", "application/json\\r\\n", "Accept", "application/json").
                when().get(uri).
                then().contentType(ContentType.JSON).extract().response();
    }

    public Response getMovieList() {
        return getRequest("https://swapi.dev/api/films/");
    }

    public Response getCharacterList() {
        return getRequest("https://swapi.dev/api/people/");
    }

    List<Response> buildResponseList(String type, int size) {
        List<Response> responses = new ArrayList<>();
        String uri_start = "https://swapi.dev/api/" + type + "/";
        String uri;
        Response tmp;
        for (int i = 1; i < size + 1; i++) {
            uri = uri_start + i + "/";
            tmp = getRequest(uri);
            if (tmp.getStatusCode() == 200) {
                responses.add(tmp);
            }
        }
        return responses;
    }

    public Response getLatestFilm() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date latest_date = sdf.parse("1900-01-01");
        Date current_date;
        Response latest_film = this.film_list.getFirst(); // need to instantiate this
        for (Response film : this.film_list) {
            current_date = sdf.parse(film.jsonPath().getString("release_date"));
            if (current_date.after(latest_date)) {
                latest_film = film;
                latest_date = current_date;
            }
        }
        System.out.println("The newest Star Wars movie is " + latest_film.jsonPath().get("title") +
                " it came out on " + latest_date.toString());
        return latest_film;
    }

    public String getTallestCharacterFromUriList(List<String> character_uris) {
        String tallest_character_name = "";
        int tallest_height = 0, current_height;
        Response character_json;
        for (String uri : character_uris) {
            character_json = RestAssured.get(uri);
            current_height = character_json.jsonPath().getInt("height");
            if (current_height > tallest_height) {
                tallest_height = current_height;
                tallest_character_name = character_json.jsonPath().getString("name");
            }
        }
        System.out.println("Tallest character in this list is " + tallest_character_name);
        return tallest_character_name;
    }

    public String getTallestCharacterFromResponseList() {
        String tallest = "";
        int tallest_height = 0, tmp;
        for (Response character : this.character_list) {
            if (character.jsonPath().get("height").equals("unknown")) continue;
            tmp = character.jsonPath().getInt("height");
            System.out.println(character.jsonPath().getString("name") + ":" + tmp);
            if (tmp > tallest_height) {
                tallest_height = tmp;
                tallest = character.jsonPath().getString("name");
            }
        }
        System.out.print("Tallest in all the galaxy is " + tallest + " at " + tallest_height);
        return tallest;
    }

}


