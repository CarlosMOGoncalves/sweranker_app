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
import android.view.ViewGroup;

import java.util.List;

import pt.cmg.sweranker.KnowledgeArea;
import pt.cmg.sweranker.KnowledgeAreaAdapter;
import pt.cmg.sweranker.R;


public class SwebokFragment extends Fragment {

    /**
     * Using parameters if anything needs to be passed to fragment
     */
    //private static final String ARG_PARAM1 = "param1";
    //private static final String ARG_PARAM2 = "param2";

    /**
     * Here as well
     */
    //private String mParam1;
    //private String mParam2;

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
        //Bundle args = new Bundle();
        //args.putString(ARG_PARAM1, param1);
        //args.putString(ARG_PARAM2, param2);
        //fragment.setArguments(args);
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
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        _myView = inflater.inflate(R.layout.fragment_swebok, container, false);

        _swebokGrid = (RecyclerView) _myView.findViewById(R.id.swebok_grid);

        KnowledgeAreaAdapter adapter = new KnowledgeAreaAdapter(this.getActivity(), _parentActivity.onSwebokFragmentInteraction());

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
        List<KnowledgeArea> onSwebokFragmentInteraction();
    }
}
