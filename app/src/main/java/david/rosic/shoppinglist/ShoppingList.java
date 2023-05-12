package david.rosic.shoppinglist;

public class ShoppingList {
    private String mNaslov;
    private boolean mShared;

    public ShoppingList(String mNaslov, boolean mShared) {
        this.mNaslov = mNaslov;
        this.mShared = mShared;
    }

    public String getmNaslov() {
        return mNaslov;
    }

    public void setmNaslov(String mNaslov) {
        this.mNaslov = mNaslov;
    }

    public boolean ismShared() {
        return mShared;
    }

    public void setmShared(boolean mShared) {
        this.mShared = mShared;
    }
}
