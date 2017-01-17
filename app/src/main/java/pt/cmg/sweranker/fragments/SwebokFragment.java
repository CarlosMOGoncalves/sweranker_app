package pt.cmg.sweranker.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import pt.cmg.sweranker.KnowledgeArea;
import pt.cmg.sweranker.R;


public class SwebokFragment extends Fragment {


    /**
     * This is a reference to the parent activity that this fragment will be attached to on onAttach()
     * It is used to communicate with it.
     */
    private OnSwebokFragmentInteractionListener _parentActivity;


    private RecyclerView _swebokGrid;
    private View _myView;


    public SwebokFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SwebokFragment.
     */
    public static SwebokFragment newInstance() {
        SwebokFragment fragment = new SwebokFragment();
        return fragment;
    }

    @Override
    public void onAttach(Context parentActivity) {
        super.onAttach(parentActivity);
        if (parentActivity instanceof OnSwebokFragmentInteractionListener) {
            _parentActivity = (OnSwebokFragmentInteractionListener) parentActivity;
        } else {
            throw new RuntimeException(parentActivity.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        _myView = inflater.inflate(R.layout.fragment_swebok, container, false);

        _swebokGrid = (RecyclerView) _myView.findViewById(R.id.swebok_grid);

        KnowledgeAreaAdapter adapter = new KnowledgeAreaAdapter(this.getActivity(), _parentActivity.loadKnowledgeAreasForSwebokFragment());

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this.getActivity(), 2);
        _swebokGrid.setLayoutManager(mLayoutManager);
        _swebokGrid.addItemDecoration(new ConstantSpacingItemDecorator(10));
        _swebokGrid.setItemAnimator(new DefaultItemAnimator());
        _swebokGrid.setAdapter(adapter);
        return _myView;
    }


    @Override
    public void onDetach() {
        super.onDetach();
        _parentActivity = null;
    }


    /**
     * This Item Decorator uses a single pixel sized spacing
     */
    public class ConstantSpacingItemDecorator extends RecyclerView.ItemDecoration {

        private int _spacingInDp;

        public ConstantSpacingItemDecorator(int spacingInDp) {
            _spacingInDp = spacingInDp;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.bottom = outRect.top = outRect.left = outRect.right = dpToPx(_spacingInDp);
        }

        /**
         * Converts dp sizes to actual pixels
         *
         * @param dp
         * @return
         */
        private int dpToPx(int dp) {
            Resources r = getResources();
            return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
        }
    }


    /**
     * Communication Interface
     */
    public interface OnSwebokFragmentInteractionListener {
        List<KnowledgeArea> loadKnowledgeAreasForSwebokFragment();

        void loadDetailedKnowledgeAreaFragment(int knowledgeAreaId);
    }


    /**
     * This Adapter transforms a list of Knowledge Areas in Views for the parent fragment recycler view.
     */
    private class KnowledgeAreaAdapter extends RecyclerView.Adapter<KnowledgeAreaAdapter.KAViewHolder> {

        private Context _context;
        private List<KnowledgeArea> _knowledgAreas;


        public KnowledgeAreaAdapter(Context context, List<KnowledgeArea> knowledgeAreas) {
            _context = context;
            _knowledgAreas = knowledgeAreas;
        }

        @Override
        public KnowledgeAreaAdapter.KAViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.swebok_card, parent, false);
            return new KnowledgeAreaAdapter.KAViewHolder(itemView);
        }


        @Override
        public void onBindViewHolder(KnowledgeAreaAdapter.KAViewHolder holder, int position) {
            KnowledgeArea knowledgeArea = _knowledgAreas.get(position);
            holder._kaName.setText(_context.getResources().getString(knowledgeArea.getNameResource()));
            holder._kaTopicCount.setText(knowledgeArea.getTopicsCount() + " " + _context.getResources().getString(R.string.topics_lowercase));
            holder._kaImage.setImageDrawable(_context.getResources().getDrawable(knowledgeArea.getImageResource(), null));
        }


        @Override
        public int getItemCount() {
            return _knowledgAreas.size();
        }


        /**
         * ViewHolder pattern to hold one of the cards
         */
        class KAViewHolder extends RecyclerView.ViewHolder {

            private ImageView _kaImage;
            private TextView _kaName;
            private TextView _kaTopicCount;

            public KAViewHolder(View view) {
                super(view);
                _kaImage = (ImageView) view.findViewById(R.id.ka_image);
                _kaName = (TextView) view.findViewById(R.id.ka_name);
                _kaTopicCount = (TextView) view.findViewById(R.id.ka_topic_number);

                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int pos = getAdapterPosition();
                        _parentActivity.loadDetailedKnowledgeAreaFragment(_knowledgAreas.get(pos).getId());
                    }
                });
            }
        }
    }

}

