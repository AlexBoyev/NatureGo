package mindcet.natureg.MainMenu_Fragments.AdminFragment;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import mindcet.natureg.R;

public class AdminAdapter extends RecyclerView.Adapter<AdminAdapter.UserHolder> {

    private ArrayList<AdminItem> userList;
    private OnItemClickListener listener;

    public interface OnItemClickListener{
        void onItemClick(int position);
        void onPromoteClick(int position);
        void onDemoteClick(int position);
        void onRemoveClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listen){
        listener = listen;
    }
    public static class UserHolder extends RecyclerView.ViewHolder{
        public ImageView image;
        public TextView userName;
        public TextView userStatus;
        public ImageView promote;
        public ImageView demote;
        public ImageView remove;

        public UserHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            image = itemView.findViewById(R.id.admin_panel_image);
            userName = itemView.findViewById(R.id.user_phone_details);
            userStatus = itemView.findViewById(R.id.admin_user_status);
            promote = itemView.findViewById(R.id.admin_promote_button);
            demote = itemView.findViewById(R.id.admin_demote_button);
            remove = itemView.findViewById(R.id.admin_delete_button);

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
            promote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onPromoteClick(position);
                        }
                    }

                }
            });
            demote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onDemoteClick(position);
                        }
                    }

                }
            });
            remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onRemoveClick(position);
                        }
                    }
                }
            });
        }

    }

    public AdminAdapter(ArrayList<AdminItem> statistics){
        userList = statistics;
    }



    @NonNull
    @Override
    public UserHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.admin_item, viewGroup, false);
        UserHolder evh = new UserHolder(v,listener);
        return evh;
    }

    @Override
    public void onBindViewHolder(@NonNull UserHolder userHolder, int i) {
        AdminItem currentItem = userList.get(i);
        userHolder.image.setImageResource(currentItem.getmImageResource());
        userHolder.userName.setText(currentItem.getUserPhone());
        userHolder.userStatus.setText(currentItem.getUserStatus());

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}
