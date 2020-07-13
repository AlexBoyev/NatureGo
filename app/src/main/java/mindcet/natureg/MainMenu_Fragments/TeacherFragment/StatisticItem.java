package mindcet.natureg.MainMenu_Fragments.TeacherFragment;

public class StatisticItem {
    private int mImageResource;
    private String hikeName;
    private String hikeInfo;

    public StatisticItem(int image, String str1, String str2){
        mImageResource = image;
        hikeName = str1;
        hikeInfo = str2;
    }

    public String getHikeInfo() {
        return hikeInfo;
    }

    public String getHikeName() {
        return hikeName;
    }

    public int getmImageResource() {
        return mImageResource;
    }
}
