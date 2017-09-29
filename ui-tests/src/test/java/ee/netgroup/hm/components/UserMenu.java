package ee.netgroup.hm.components;

import org.openqa.selenium.By;
import ee.netgroup.hm.helpers.Helpers;

public class UserMenu extends Component{
	
	private static By loginButton = By.id("header-login-icon");
	private static By userMenuIcon = By.id("header-user-menu-icon");
	private static By userMenuLogout = By.id("user-menu-logout");
	private static By userTour = By.xpath("//body[@class='tour-modal-is-showing md-dialog-is-showing']");
	private static By closeTour = By.xpath("//button[@id='tour-cancel-button']");
	private By currentUser = By.cssSelector("a#user-menu-dashboard span");
	
	
	public static UserMenu logoutIfLoggedIn() {
	
		if (!getDriver().findElement(loginButton).isDisplayed()){
			getDriver().findElement(userMenuIcon).click();
			Helpers.waitForSeconds(1000);
			getDriver().findElement(userMenuLogout).click();
		}	
		return new UserMenu();
	}

	public LoginPopUp clickLogin() {
		getDriver().findElement(loginButton).click();
		return new LoginPopUp();
	}

	public static UserMenu clickProfileIcon() {
		
		if (Helpers.elementExists(userTour) == true){
			getDriver().findElement(closeTour).click();
			//Helpers.waitForSeconds(3000);
		}
	
		Helpers.waitForVisibility(userMenuIcon);
		getDriver().findElement(userMenuIcon).click();
		Helpers.waitForSeconds(1000);
		return new UserMenu();
	}

	public String getUserName() {
		return getDriver().findElement(currentUser).getText();
	}

	public LeftMenu clickMyThings() {
		return LeftMenu.clickMyThings();
	}

	
}
