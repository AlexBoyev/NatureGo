package mindcet.natureg.Map;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import mindcet.natureg.R;

public class HistoryItemAdapter extends RecyclerView.Adapter<HistoryItemAdapter.HistoryViewHolder> {


    private ArrayList<HikeHistoryItem> historyItemArrayList;
    private OnItemClickListener mListener;
    public interface OnItemClickListener{
        void OnItemClick(int position);
        void OnDeleteClick(int position);

    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;

    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder{

        public ImageView myImageView;
        public TextView hike_name;
        public TextView hike_data;
        public ImageView deleteImage;

        public HistoryViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            myImageView = itemView.findViewById(R.id.imageView);
            hike_name = itemView.findViewById(R.id.hike_name);
            hike_data = itemView.findViewById(R.id.hike_data);
            deleteImage = itemView.findViewById(R.id.icon_delete);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listener!= null){
                        int position = getAdapterPosition();
                        if(position!= RecyclerView.NO_POSITION){
                            listener.OnItemClick(position);
                        }
                    }
                }
            });
            deleteImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listener!= null){
                        int position = getAdapterPosition();
                        if(position!= RecyclerView.NO_POSITION){
                            listener.OnDeleteClick(position);
                        }
                    }
                }
            });
        }
    }

    public HistoryItemAdapter(ArrayList<HikeHistoryItem> historyItems){
        historyItemArrayList = historyItems;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View item_view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.map_history_item,
                viewGroup,false);
        HistoryViewHolder hvh = new HistoryViewHolder(item_view,mListener);
        return hvh;
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder historyViewHolder, int i) {
        HikeHistoryItem hikeHistoryItem = historyItemArrayList.get(i);
        historyViewHolder.myImageView.setImageResource(hikeHistoryItem.getImageResource());
        historyViewHolder.hike_name.setText(hikeHistoryItem.getHikeName());
        historyViewHolder.hike_data.setText(hikeHistoryItem.getHikeData());
    }

    @Override
    public int getItemCount() {
        return historyItemArrayList.size();
    }
}
