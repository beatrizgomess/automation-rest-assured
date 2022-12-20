package User;

import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.example.entities.user.User;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
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
        requestSpecification = given().header("api-key", "special-key")
                //.header("Content-Type","application/json");
                .contentType(ContentType.JSON);
    }

    @Test
    public void CreateNewUser_WithValidData_ReturnOk(){
       requestSpecification.body(user)
               .when().post("/user")
               .then()
               .assertThat().statusCode(200).and()
               .body("code", equalTo(200))
               .body("type", equalTo("unknow"))
               .body("message", isA(String.class))
               .body("size()", equalTo(3));
    }



}
