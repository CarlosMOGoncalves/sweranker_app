package pt.cmg.sweranker.degrees;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

        if (position > 0) {
            holder._degreeImage.setAlpha(0.2f);
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

        private ImageView _degreeImage;
        private TextView _degreeName;
        private TextView _universityName;

        public DegreeViewHolder(View view) {
            super(view);
            _degreeImage = (ImageView) view.findViewById(R.id.university_image);
            _degreeName = (TextView) view.findViewById(R.id.degree_name);
            _universityName = (TextView) view.findViewById(R.id.university_name);

            view.setOnClickListener(v -> {
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
