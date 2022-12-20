package User;

import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.example.entities.user.User;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static io.restassured.config.LogConfig.logConfig;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.core.Is.isA;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserTest {

    private static User user;
    public static RequestSpecification requestSpecification;
    public static Faker faker;
    @BeforeAll
    public static void setup(){
        RestAssured.baseURI ="https://petstore.swagger.io/v2";

        faker = new Faker();

        user = new User(faker.name().username(),
                faker.name().firstName(),
                faker.name().lastName(),
                faker.internet().safeEmailAddress(),
                faker.internet().password(8,10),
                faker.phoneNumber().toString());

    }


    @BeforeEach
    void setRequest(){
        requestSpecification = given().config(RestAssured.config()
                        .logConfig(logConfig()
                                .enableLoggingOfRequestAndResponseIfValidationFails()))
                .header("api-key", "special-key")
                //.header("Content-Type","application/json");
                .contentType(ContentType.JSON);
    }

    @Test
    @Order(1)
    public void CreateNewUser_WithValidData_ReturnOk(){
       requestSpecification.body(user)
               .when().post("/user")
               .then()
               .assertThat().statusCode(200).and()
               .body("code", equalTo(200))
               .body("type", equalTo("unknown"))
               .body("message", isA(String.class))
               .body("size()", equalTo(3));
    }

    @Test
    @Order(2)
    void getLogin_valid_returnOk(){
        requestSpecification
                .param("username", user.getUsername())
                .param("password", user.getPassword())
                .when()
                .get("user/login")
                .then()
                .assertThat()
                .statusCode(200)
                .and().time(lessThan(2000L))
                .and().body(JsonSchemaValidator.matchesJsonSchemaInClasspath("loginResponseSchema.json"));

    }

    @Test
    @Order(3)
    void getUserByUsername_userIsValid_returnOk(){
        requestSpecification
                .when()
                .get("/user/" + user.getUsername())
                .then()
                .assertThat().statusCode(200).and().time(lessThan(2000L))
                .and().body("firstname", equalTo(user.getFirstname()));

        //TO DO: Schema Validation

    }

    @Test
    @Order(4)
    void deleteUser_userExits_Return200(){
        requestSpecification
                .when()
                .get("/user/" + user.getUsername())
                .then()
                .assertThat().statusCode(200).and().time(lessThan(2000L))
                .log();

    }


    @Test
    @Order(5)
    void createUser_withInvalidBody_returnBadRequest(){
        Response response = requestSpecification
                .body("teste")
                .when()
                .post("/user")
                .then()
                .extract().response();

        Assertions.assertNotNull(response);
        Assertions.assertEquals(400, response.statusCode());
        Assertions.assertEquals(true, response.getBody().asPrettyString().contains("unknown"));
        Assertions.assertEquals(3, response.body().jsonPath().getMap("$").size());
    }

}
