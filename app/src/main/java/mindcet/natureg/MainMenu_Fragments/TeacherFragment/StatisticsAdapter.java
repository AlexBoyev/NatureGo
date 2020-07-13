package mindcet.natureg.MainMenu_Fragments.TeacherFragment;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import mindcet.natureg.R;

public class StatisticsAdapter extends RecyclerView.Adapter<StatisticsAdapter.StatisticsHolder> {

    private ArrayList<StatisticItem> statisticsList;
    private OnItemClickListener listener;
    public interface OnItemClickListener{
        void onItemClick(int position);
    }
    public void setOnItemClickListener(OnItemClickListener listen){
        listener = listen;
    }
    public static class StatisticsHolder extends RecyclerView.ViewHolder{
        public ImageView image;
        public TextView hikeName;
        public TextView hikeInfo;

        public StatisticsHolder(@NonNull View itemView,OnItemClickListener listener) {
            super(itemView);
            image = itemView.findViewById(R.id.hike_statistics_image);
            hikeName = itemView.findViewById(R.id.statistics_hike_name);
            hikeInfo = itemView.findViewById(R.id.statistic_hike_info);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onItemClick(position);
                        }
                    }
                }
            });

        }

    }

    public StatisticsAdapter(ArrayList<StatisticItem> statistics){
        statisticsList = statistics;
    }



    @NonNull
    @Override
    public StatisticsHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.statistics_item, viewGroup, false);
        StatisticsHolder evh = new StatisticsHolder(v,listener);
        return evh;
    }

    @Override
    public void onBindViewHolder(@NonNull StatisticsHolder StatisticsHolder, int i) {
        StatisticItem currentItem = statisticsList.get(i);
        StatisticsHolder.image.setImageResource(currentItem.getmImageResource());
        StatisticsHolder.hikeName.setText(currentItem.getHikeName());
        StatisticsHolder.hikeInfo.setText(currentItem.getHikeInfo());

    }

    @Override
    public int getItemCount() {
        return statisticsList.size();
    }
}
