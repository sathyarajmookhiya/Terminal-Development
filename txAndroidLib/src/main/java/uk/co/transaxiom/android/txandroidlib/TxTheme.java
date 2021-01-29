package uk.co.transaxiom.android.txandroidlib;

public class TxTheme {

	public static final String THEME_PREFERENCES = "ThemePreferences";
	public static final String THEME_NAME = "ThemeName";
	public static final String THEME_SLOGAN = "ThemeSlogan";
	public static final String COLOR_MAIN = "ColorMain";
	public static final String COLOR_DARK = "ColorDark";
	public static final String COLOR_HIGHLIGHT = "ColorHighlight";
	public static final String COLOR_BACKGROUND = "ColorBackground";
	public static final String COLOR_PAGE = "ColorPage";
	public static final String LOGO_LOCATION = "LogoLocation";
	
	private String name;
	private String slogan;
	private String colorMain;
	private String colorHighlight;
	private String colorDark;
	private String backgroundColor;
	private String pageColor;
	private String logoLocation;
	
	public TxTheme (){
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getSlogan() {
		return slogan;
	}

	public void setSlogan(String slogan) {
		this.slogan = slogan;
	}

	public String getColorMain() {
		return colorMain;
	}

	public void setColorMain(String colorMain) {
		this.colorMain = colorMain;
	}

	public String getColorHighlight() {
		return colorHighlight;
	}

	public void setColorHighlight(String colorLight) {
		this.colorHighlight = colorLight;
	}

	public String getColorDark() {
		return colorDark;
	}

	public void setColorDark(String colorDark) {
		this.colorDark = colorDark;
	}

	public String getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(String backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public String getPageColor() {
		return pageColor;
	}

	public void setPageColor(String pageColor) {
		this.pageColor = pageColor;
	}

	public String getLogoLocation() {
		return logoLocation;
	}

	public void setLogoLocation(String logoLocation) {
		this.logoLocation = logoLocation;
	}


}
