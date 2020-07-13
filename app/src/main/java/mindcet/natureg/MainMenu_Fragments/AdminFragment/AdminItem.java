package mindcet.natureg.MainMenu_Fragments.AdminFragment;

public class AdminItem {
    private int mImageResource;
    private String userPhone;
    private String userStatus;

    public AdminItem(int image, String str1, String str2){
        mImageResource = image;

        userPhone = str1;
        userStatus = str2;
    }


    public String getUserPhone(){
        return userPhone;
    }

    public String getUserStatus(){
        return userStatus;
    }

    public int getmImageResource() {
        return mImageResource;
    }

    public void setUserStatus(String newStatus){
        userStatus = newStatus;
    }
}
