package AutomationTest.BrightTalkTest;

import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import static AutomationTest.BrightTalkTest.HomePage.homePage;
import static org.junit.Assert.assertEquals;

import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;

import static AutomationTest.BrightTalkTest.HTTPMethods.getResponseStatus;
import static AutomationTest.BrightTalkTest.HTTPMethods.isResponseContainsKey;
import static AutomationTest.BrightTalkTest.HTTPMethods.getJsonResponseAsString;
import static AutomationTest.BrightTalkTest.HTTPMethods.getResponsePostCall;
import static AutomationTest.BrightTalkTest.HTTPMethods.validateResponse;
import static AutomationTest.BrightTalkTest.HTTPMethods.POSTRequestCreate;



public class StepDefinition {
	// declaring global variables
	String baseUrl = "https://reqres.in/api";
	String getUsersPaginationPath = "/users?page=1";
	String invalidUserPath = "/user/55";
	String delayedResponsePath="/users?delay=3";
	int statusCode;
	String responseData;
	String delayedResponse;
	@Given("^I am on the home page$")
	public void iAmOnTheHomePage() {

		homePage();
	}

	//  Hit the url and get Status code
	@Given("^I get the default list of users for on 1st page$")
	public void getDefaultListOfUsers() throws JSONException {
		 statusCode = getResponseStatus(baseUrl + getUsersPaginationPath);		
	}

	// verifying the status code , if it is 200OK, verifying for a key element
	@When("^I get the list of all users$")
	public void getFirstPageListOfUsers(DataTable dt) {
		List<String> list = dt.asList(String.class);
		assertEquals(list.get(1), String.valueOf(statusCode));
		if (statusCode == 200) {
			boolean isFound = isResponseContainsKey(baseUrl + getUsersPaginationPath, "first_name");
			Assert.assertTrue(isFound);
		}

	}

	// converting string to JSONObject
	// As the response has paginated data, marking case as pass when
	// total count=length of user array* number of pages
	@Then("^I should see total users count equals to number of user ids$")
	public void verifyUserCountEqualsuserId() throws JSONException {

		String response = getJsonResponseAsString(baseUrl + getUsersPaginationPath);
		JSONObject json = new JSONObject(response);
		String expectedUserCount = json.get("total").toString();

		int userPage1 = json.getJSONArray("data").length();
		String totalPages = json.get("total_pages").toString();
		int totalPagesInt = Integer.parseInt(totalPages);

		int UserCountInt = totalPagesInt * userPage1;
		String actualUserCount = String.valueOf(UserCountInt);

		System.out.print("Expected:" + expectedUserCount);
		System.out.print("Actual:" + actualUserCount);
        assertEquals(expectedUserCount,actualUserCount);
		if (expectedUserCount.equalsIgnoreCase(actualUserCount)) {
			System.out.println("Total user count is equal to no of user ID's");
		} else {
			System.out.println("Total user count is not equal to no of user ID's");
		}
	}

//searching for an object which is not present 
	@Given("^I make a search for user with below ID$")
	public void searchUser(DataTable dt) throws JSONException {
		List<String> list = dt.asList(String.class);
		  responseData=getJsonResponseAsString(baseUrl + "/user/"+list.get(1));
	}

//validating error
	@Then("^I receive error code in response$")
	public void validateErrorCode(DataTable dt) {
		List<String> list = dt.asList(String.class);		
assertEquals(list.get(1),responseData);
		
	}

//creating a user, handled in HTTPMethods
	@Given("I create user with following (.*) (.*)")
	public void createUserWithData(String name, String job) throws JSONException {
		POSTRequestCreate(baseUrl, name, job);
	}

//verifying the response after posting the data
	@Then("response should contain folowing data")
	public void verifyUser(DataTable dt) throws JSONException {
		List<String> list = dt.asList(String.class);
		String postResponse=getResponsePostCall();
		Assert.assertTrue(postResponse.contains(list.get(0)) || postResponse.contains(list.get(1)));
	}

//Validating login functionality, using data table to pass data
	@Given("^I login succesfully with following data$")
	public void loginVerify(DataTable dt) {
		List<String> list = dt.asList(String.class);
		System.out.println("Username - " + list.get(0));
		System.out.println("Password - " + list.get(1));

		validateResponse(baseUrl, list.get(0), list.get(1));

	}

//verify user is present after loading a page for some time
	@Given("^I wait for user list to load$")
	public void waitForUserListToLoad(DataTable dt) throws InterruptedException {
		List<String> list = dt.asList(String.class);
		 Long MAX_TIMEOUT = Long.valueOf(list.get(1));
		 delayedResponse = getJsonResponseAsString(baseUrl + delayedResponsePath);	
	}

//verifying each ID is unique 
	@Then("^I should see that every user has a unique id$")
	public void uniqueIdVerify() throws JSONException {

	
		JSONObject json = new JSONObject(delayedResponse);
		int length = json.getJSONArray("data").length();
		System.out.println(json.getJSONArray("data").get(2).toString().substring(1, 20));
		for (int i = 0; i < length - 1; i++) {
			Assert.assertFalse(json.getJSONArray("data").get(i).toString().substring(1, 20)
					.equals(json.getJSONArray("data").get(i + 1).toString().substring(1, 20)));

			
//			if (json.getJSONArray("data").get(i).toString().substring(1, 20)
//					.equals(json.getJSONArray("data").get(i + 1).toString().substring(1, 20))) {
//				System.out.print("ID is not unique");
//			} else {
//				System.out.print("ID is  unique");
//			}
		}
	}

}
