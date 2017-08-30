package pt.cmg.sweranker.degrees;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import pt.cmg.sweranker.R;

/**
 * Created by Carlos on 16/02/2017.
 * <p>
 * This Adapter transforms a list of Knowledge Areas in Views for the parent fragment recycler view.
 */
public class DegreeAdapter extends RecyclerView.Adapter<DegreeAdapter.DegreeViewHolder> {

    private Context _context;
    private List<Degree> _degrees;
    private OnDegreeAdapterListener _listener;


    public DegreeAdapter(Context context, List<Degree> degrees, OnDegreeAdapterListener listener) {
        _context = context;
        _degrees = degrees;
        _listener = listener;
    }

    @Override
    public DegreeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.degree_card, parent, false);
        return new DegreeViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(DegreeViewHolder holder, int position) {
        Degree degree = _degrees.get(position);

        holder._degreeName.setText(_context.getResources().getString(degree.getNameResource()));
        holder._degreeName.setTransitionName("degree_name" + position);

        holder._universityName.setText(_context.getResources().getString(degree.getUniversityResource()));
        holder._universityName.setTransitionName("university_name" + position);

        holder._degreeImage.setImageDrawable(_context.getResources().getDrawable(degree.getImageResource(), null));
        holder._degreeImage.setTransitionName("degree_image" + position);

        // TODO: as more degrees start to become available check this!
        if (position > 0) {
            holder._degreeImage.setAlpha(0.2f);

            // Ok, here I basically create a TextView to overlay on top of the degrees that are not yet available. Neat.
            TextView comingSoonText = new TextView(_context);
            comingSoonText.setText(_context.getString(R.string.coming_soon));
            comingSoonText.setTextSize(_context.getResources().getDimensionPixelSize(R.dimen.degree_master_comming_soon_text_size));
            comingSoonText.setRotation(-45f);
            comingSoonText.setTextColor(ContextCompat.getColor(_context, R.color.materialNegative));
            comingSoonText.setGravity(Gravity.CENTER);

            ViewGroup.LayoutParams params = holder._imageContainer.getLayoutParams();
            params.height = FrameLayout.LayoutParams.MATCH_PARENT;
            params.width = FrameLayout.LayoutParams.MATCH_PARENT;

            holder._imageContainer.addView(comingSoonText, params);

        }
    }


    @Override
    public int getItemCount() {
        return _degrees.size();
    }


    /**
     * ViewHolder pattern to hold one of the cards
     */
    class DegreeViewHolder extends RecyclerView.ViewHolder {

        private FrameLayout _imageContainer;
        private ImageView _degreeImage;
        private TextView _degreeName;
        private TextView _universityName;

        public DegreeViewHolder(View view) {
            super(view);
            _imageContainer = view.findViewById(R.id.degree_image_container);
            _degreeImage = view.findViewById(R.id.university_image);
            _degreeName = view.findViewById(R.id.degree_name);
            _universityName = view.findViewById(R.id.university_name);

            view.setOnClickListener(v -> {
                // TODO: as more degrees start to become available check this!
                if (getAdapterPosition() > 0) {
                    Toast.makeText(_context, "Coming soon!", Toast.LENGTH_SHORT).show();
                } else {
                    _listener.loadDetailedDegreeFragment(v, _degrees.get(getAdapterPosition()));
                }
            });

        }
    }


    public interface OnDegreeAdapterListener {

        /**
         * Loads the detail degree fragment of the given Degree id.
         */
        void loadDetailedDegreeFragment(View rootView, Degree degree);
    }
}
