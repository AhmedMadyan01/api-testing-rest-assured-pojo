package tests;

import api.driver.APIActions;
import io.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static api.driver.RequestMethod.GET;
import static api.driver.RequestMethod.POST;

public class GetBill {
    String baseURI = ("https://api.non-prod.vf-itaap.engineering.vodafone.com");
    String endPoint = ("/bill-analysis-js/api/v1/bill");

    private static WebDriver webDriver;
    private static String codeValue;
    private static String bearerToken;


    //        @BeforeTest
    @BeforeTest
    public void generateAndExtractCode() throws InterruptedException {
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.setAcceptInsecureCerts(true);
        chromeOptions.setHeadless(true);

        By USERNAME_TEXT_BOX = By.id("identifier");
        By PASSWORD_TEXT_BOX = By.id("password");
        By CONTINUE_BUTTON = By.id("continueButton");
        By COOKIES_OK_BUTTON = By.id("cookieOkButton");

        webDriver = new ChromeDriver(chromeOptions);
        webDriver.get("https://hub.dev.external.nonprod.id-euc1.aws.cps.vodafone.com/oidc/authorize?login_hint=OPCO%3AIE&acr_values=urn%3Avodafone%3Aloa%3Asilver&scope=openid+phone+offline_access+profile&response_type=code&redirect_uri=https%3A%2F%2Fexample.com%2Ftest&state=d1ccf7f8-6033-45c7-8382-3609586ba8fc&nonce=728e5fa1-fa98-4394-a757-a5b463ba4120&client_id=OneApp");
        Thread.sleep(5000);
        webDriver.findElement(COOKIES_OK_BUTTON).click();
        webDriver.findElement(USERNAME_TEXT_BOX).sendKeys("0878004032");
        webDriver.findElement(PASSWORD_TEXT_BOX).sendKeys("Aa123456!");
        webDriver.findElement(CONTINUE_BUTTON).click();
        Thread.sleep(5000);

        String url = webDriver.getCurrentUrl();
        String[] code = StringUtils.substringsBetween(url, "code=", "&state");

        codeValue = Arrays.toString(code).replaceAll("[\\[\\]]", "");
        System.out.println("Code= " + codeValue);
    }

    @BeforeTest(dependsOnMethods = "generateAndExtractCode")
    public void generateAndExtractBearerToken() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Accept", "application/json");


        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "authorization_code");
        body.put("client_id", "OneApp");
        body.put("client_secret", "dve3fgpo91az22one");
        body.put("redirect_uri", "https://example.com/test");
        body.put("code", codeValue);

        Response response =
                APIActions
                        .setRequestSpecifications()
                        .setRequestMethod(POST)
                        .setBaseUri("https://hub.dev.external.nonprod.id-euc1.aws.cps.vodafone.com")
                        .setBasePath("/oidc/token")
                        .addHeaders(headers)
                        .addFormParams(body)
                        .sendRequest();
        bearerToken = "Bearer " + response.getBody().jsonPath().get("id_token");
        System.out.println("Access token: " + bearerToken);
    }


    @Test
    public void sendRequest() {
        Map<String, String> query_map = new HashMap<>();
        query_map.put("billingAccount.id", "BI33445566");
        query_map.put("limit", "3");

        Map<String, String> headers_map = new HashMap<>();
        headers_map.put("vf-country-code", "IE");
        headers_map.put("vf-project", "ONEAPP");
        headers_map.put("Cookie", "{{Cookie}}");
        headers_map.put("Authorization", bearerToken);
        headers_map.put("CF-Access-Client-Secret", "6423bc67b7f9579917b01ba92590a7ffc2144800aff6e32864c5b2459c603b75");
        headers_map.put("CF-Access-Client-Id", "e022a18ccb80b93937a395cf7e539d24.access");

        Response response =
                APIActions
                        .setRequestSpecifications()
                        .setRequestMethod(GET)
                        .setBaseUri(baseURI)
                        .setBasePath(endPoint)
                        .addHeaders(headers_map)
                        .addQueryParams(query_map)
                        .sendRequest();

        System.out.println(response.getBody().asString());
    }
}