package freeze.in.co.ufily;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;


/**
 * Created by rtiragat on 8/11/2015.
 */
public class UfilyPagerAdapter extends FragmentPagerAdapter {

    private static final CharSequence Titles[] = {"Funny Face Maker","Funny Faces","Famous Ufily"};

    public UfilyPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fm=null;
        switch(i)
        {
            case 0:
                fm = new UfilyFunnyFaceMaker();
                break;
            case 1:
                fm = new UfilyFunnyFacesGridView();
                break;
            //case 2:
              //  fm = new UfilyFamousFragment();
                //break;
            default:
                fm = new UfilyEditFragment();
        }

        return fm;
    }


    @Override
    public int getCount() {
        return 2;
    }



    @Override
    public CharSequence getPageTitle(int position) {

        return Titles[position];
    }
}
