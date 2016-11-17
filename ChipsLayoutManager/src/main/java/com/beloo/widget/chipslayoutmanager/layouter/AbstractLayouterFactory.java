package com.beloo.widget.chipslayoutmanager.layouter;

import android.graphics.Rect;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager;
import com.beloo.widget.chipslayoutmanager.gravity.RowGravityModifiersFactory;
import com.beloo.widget.chipslayoutmanager.layouter.breaker.IBreakerFactory;
import com.beloo.widget.chipslayoutmanager.cache.IViewCacheStorage;
import com.beloo.widget.chipslayoutmanager.layouter.criteria.VerticalDefaultCriteriaFactory;
import com.beloo.widget.chipslayoutmanager.layouter.criteria.DisappearingCriteriaFactory;
import com.beloo.widget.chipslayoutmanager.layouter.criteria.ICriteriaFactory;
import com.beloo.widget.chipslayoutmanager.layouter.criteria.InfiniteCriteria;
import com.beloo.widget.chipslayoutmanager.layouter.placer.DisappearingPlacerFactory;
import com.beloo.widget.chipslayoutmanager.layouter.placer.IPlacerFactory;
import com.beloo.widget.chipslayoutmanager.layouter.placer.RealPlacerFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractLayouterFactory {
    ChipsLayoutManager layoutManager;
    private IViewCacheStorage cacheStorage;

    @IntRange(from = 0)
    private int additionalRowsCount;

    private List<ILayouterListener> layouterListeners = new ArrayList<>();

    private int additionalHeight;

    private IBreakerFactory breakerFactory;

    AbstractLayouterFactory(ChipsLayoutManager layoutManager, IViewCacheStorage cacheStorage, IBreakerFactory breakerFactory) {
        this.cacheStorage = cacheStorage;
        this.layoutManager = layoutManager;
        this.breakerFactory = breakerFactory;
    }

    public void addLayouterListener(@Nullable ILayouterListener layouterListener) {
        if (layouterListener != null) {
            layouterListeners.add(layouterListener);
        }
    }

    public void setAdditionalHeight(@IntRange(from = 0) int additionalHeight) {
        if (additionalHeight < 0) throw new IllegalArgumentException("additional height can't be negative");
        this.additionalHeight = additionalHeight;
    }

    public void setAdditionalRowsCount(int additionalRowsCount) {
        this.additionalRowsCount = additionalRowsCount;
    }

    private int getAdditionalRowsCount() {
        return additionalRowsCount;
    }

    private int getAdditionalHeight() {
        return additionalHeight;
    }

    abstract AbstractLayouter.Builder createBackwardBuilder();
    abstract AbstractLayouter.Builder createForwardBuilder();
    abstract Rect createOffsetRectForBackwardLayouter(Rect anchorRect);
    abstract Rect createOffsetRectForForwardLayouter(Rect anchorRect);

    @NonNull
    private AbstractLayouter.Builder fillBasicBuilder(AbstractLayouter.Builder builder) {
        return builder.layoutManager(layoutManager)
                .canvas(new Square(layoutManager))
                .childGravityResolver(layoutManager.getChildGravityResolver())
                .cacheStorage(cacheStorage)
                .gravityModifiersFactory(new RowGravityModifiersFactory())
                .addLayouterListeners(layouterListeners);
    }

    @NonNull
    public final ILayouter getUpLayouter(@Nullable Rect anchorRect) {
        ICriteriaFactory criteriaFactory = VerticalDefaultCriteriaFactory.newBuilder()
                .additionalHeight(getAdditionalHeight())
                .additionalRowCount(getAdditionalRowsCount())
                .build();

        IPlacerFactory placerFactory = new RealPlacerFactory(layoutManager);

        return fillBasicBuilder(createBackwardBuilder())
                .offsetRect(createOffsetRectForBackwardLayouter(anchorRect))
                .breaker(breakerFactory.createBackwardRowBreaker())
                .finishingCriteria(criteriaFactory.getUpFinishingCriteria())
                .placer(placerFactory.getAtStartPlacer())
                .build();
    }

    @NonNull
    public final ILayouter getDownLayouter(@Nullable Rect anchorRect) {
        ICriteriaFactory criteriaFactory = VerticalDefaultCriteriaFactory.newBuilder()
                .additionalHeight(getAdditionalHeight())
                .additionalRowCount(getAdditionalRowsCount())
                .build();

        IPlacerFactory placerFactory = new RealPlacerFactory(layoutManager);

        return fillBasicBuilder(createForwardBuilder())
                .offsetRect(createOffsetRectForForwardLayouter(anchorRect))
                .breaker(breakerFactory.createForwardRowBreaker())
                .finishingCriteria(criteriaFactory.getDownFinishingCriteria())
                .placer(placerFactory.getAtEndPlacer())
                .build();
    }

    @NonNull
    public final ILayouter buildDisappearingDownLayouter(@NonNull ILayouter layouter) {
        ICriteriaFactory criteriaFactory = new DisappearingCriteriaFactory(getAdditionalRowsCount());
        IPlacerFactory placerFactory = new DisappearingPlacerFactory(layoutManager);

        AbstractLayouter abstractLayouter = (AbstractLayouter) layouter;
        abstractLayouter.setFinishingCriteria(criteriaFactory.getDownFinishingCriteria());
        abstractLayouter.setPlacer(placerFactory.getAtEndPlacer());

        return abstractLayouter;
    }

    @NonNull
    public final ILayouter buildDisappearingUpLayouter(@NonNull ILayouter layouter) {
        ICriteriaFactory criteriaFactory = new DisappearingCriteriaFactory(getAdditionalRowsCount());
        IPlacerFactory placerFactory = new DisappearingPlacerFactory(layoutManager);

        AbstractLayouter abstractLayouter = (AbstractLayouter) layouter;
        abstractLayouter.setFinishingCriteria(criteriaFactory.getUpFinishingCriteria());
        abstractLayouter.setPlacer(placerFactory.getAtEndPlacer());

        return abstractLayouter;
    }


    @NonNull
    public ILayouter buildInfiniteLayouter(ILayouter layouter) {
        ((AbstractLayouter)layouter).setFinishingCriteria(new InfiniteCriteria());
        return layouter;
    }
}
