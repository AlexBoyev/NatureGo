package mindcet.natureg.MainMenu_Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import mindcet.natureg.R;
import mindcet.natureg.Utilities.HideSysteUI;

public class Information_Fragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View RootView = inflater.inflate(R.layout.fragment_information, container, false);
        TextView link = (TextView)RootView.findViewById(R.id.info_id);
        link.setMovementMethod(LinkMovementMethod.getInstance());
        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HideSysteUI.hideSystemUI(getActivity());
            }
        });
        return RootView;

    }
}
