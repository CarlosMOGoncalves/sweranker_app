package pt.cmg.sweranker.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import pt.cmg.sweranker.R;
import pt.cmg.sweranker.degrees.Degree;
import pt.cmg.sweranker.ui.ConstantSpacingItemDecorator;


public class DegreesFragment extends Fragment {

    /**
     * This is a reference to the parent activity that this fragment will be attached to on onAttach()
     * It is used to communicate with it.
     */
    private DegreesFragmentInteractionListener _parentActivity;

    private RecyclerView _degreesGrid;
    private View _myRootView;


    public DegreesFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DegreesFragment.
     */
    public static DegreesFragment newInstance() {
        DegreesFragment fragment = new DegreesFragment();
        return fragment;
    }

    /**
     * Communication Interface
     */
    public interface DegreesFragmentInteractionListener {

        List<Degree> loadDegreesForFragment();

        void loadDetailedDegreeFragment(View v, int degreeId);

    }


    @Override
    public void onAttach(Context parentActivity) {
        super.onAttach(parentActivity);
        if (parentActivity instanceof DegreesFragmentInteractionListener) {
            _parentActivity = (DegreesFragmentInteractionListener) parentActivity;
        } else {
            throw new RuntimeException(parentActivity.toString() + " must implement DegreesFragmentInteractionListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        _myRootView = inflater.inflate(R.layout.degrees_grid_fragment, container, false);

        _degreesGrid = (RecyclerView) _myRootView.findViewById(R.id.degrees_grid);

        DegreeAdapter adapter = new DegreeAdapter(this.getActivity(), _parentActivity.loadDegreesForFragment());

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this.getActivity(), 2);
        _degreesGrid.setLayoutManager(mLayoutManager);
        _degreesGrid.addItemDecoration(new ConstantSpacingItemDecorator(this.getActivity(),
                10,
                ConstantSpacingItemDecorator.Side.LEFT,
                ConstantSpacingItemDecorator.Side.RIGHT,
                ConstantSpacingItemDecorator.Side.ALL_SIDES));
        _degreesGrid.setItemAnimator(new DefaultItemAnimator());
        _degreesGrid.setAdapter(adapter);

        return _myRootView;
    }


    @Override
    public void onDetach() {
        super.onDetach();
        _parentActivity = null;
    }


    /**
     * This Adapter transforms a list of Knowledge Areas in Views for the parent fragment recycler view.
     */
    private class DegreeAdapter extends RecyclerView.Adapter<DegreeAdapter.DegreeViewHolder> {

        private Context _context;
        private List<Degree> _degrees;


        public DegreeAdapter(Context context, List<Degree> degrees) {
            _context = context;
            _degrees = degrees;
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

                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int pos = getAdapterPosition();
                        _parentActivity.loadDetailedDegreeFragment(v, _degrees.get(pos).getId());
                    }
                });
            }


        }
    }

}
