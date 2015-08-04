package ba.glaboratory.adem.model;

/**
 * Created by Adem on 6.10.2014.
 */
public class SpinnerNavItem {

    private String title;
    private int icon;

    public SpinnerNavItem(String title, int icon) {
        this.title = title;
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public int getIcon() {
        return icon;
    }
}
