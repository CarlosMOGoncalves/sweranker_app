package pt.cmg.sweranker.degrees;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import pt.cmg.sweranker.R;
import pt.cmg.sweranker.ui.ConstantSpacingItemDecorator;
import pt.cmg.sweranker.ui.UnderlineDividerItemDecorator;

/**
 * Created by Carlos on 09/02/2017.
 * <p>
 * This is literally just a two static pages adapter, one for each of the two tabs of the layout.
 * Actually using two fragments by now was obviously too much for what was needed.
 */

public class DegreeViewPagerAdapter extends PagerAdapter {

    private Context _context;
    private Degree _degree;
    private OnDegreeClassItemSelected _itemClickedListener;

    /**
     * Implement this interface if you want to listen to any selected item on the degree class listener
     */
    public interface OnDegreeClassItemSelected {
        void onDegreeClassClicked(int degreeId, String degreeClassId);
    }

    public DegreeViewPagerAdapter(Context context, Degree degree, OnDegreeClassItemSelected listener) {
        _itemClickedListener = listener;
        _degree = degree;
        _context = context;
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        LayoutInflater inflater = LayoutInflater.from(_context);

        ViewGroup layout;
        if (position == 0) {
            layout = (ViewGroup) inflater.inflate(R.layout.degree_basic_info, collection, false);
            TextView degreeDescription = (TextView) layout.findViewById(R.id.degree_description);
            degreeDescription.setText(_context.getResources().getString(_degree.getDescriptionResource()));
            collection.addView(layout);
        } else {
            layout = (ViewGroup) inflater.inflate(R.layout.degree_program_list, collection, false);
            RecyclerView programList = (RecyclerView) layout.findViewById(R.id.program_list);
            initialiseProgramList(programList);
            collection.addView(layout);
        }

        return layout;
    }


    private void initialiseProgramList(RecyclerView degreeProgramList) {
        RecyclerView.LayoutManager linearLayoutManager = new LinearLayoutManager(_context, LinearLayoutManager.VERTICAL, false);
        degreeProgramList.setLayoutManager(linearLayoutManager);

        degreeProgramList.addItemDecoration(new ConstantSpacingItemDecorator(_context, 2, ConstantSpacingItemDecorator.Side.BOTTOM));
        degreeProgramList.addItemDecoration(new UnderlineDividerItemDecorator.Builder(_context,
                ContextCompat.getColor(_context, R.color.darkerBackground),
                1)
                .targetViewHolderClass(DegreeProgramAdapter.DegreeClassViewHolder.class)
                .build());
        degreeProgramList.setItemAnimator(new DefaultItemAnimator());

        DegreeProgramAdapter adapter = new DegreeProgramAdapter(_context, _degree, _itemClickedListener);
        degreeProgramList.setAdapter(adapter);
    }


    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return _context.getString(R.string.info);
        }

        return _context.getString(R.string.program);
    }
}
