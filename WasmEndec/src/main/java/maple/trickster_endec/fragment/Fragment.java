package maple.trickster_endec.fragment;

import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import maple.trickster_endec.Identifier;
import maple.trickster_endec.endecs.EndecTomfoolery;


public interface Fragment  {
    final int MAX_WEIGHT = 64000;

    @SuppressWarnings("unchecked")
    final StructEndec<Fragment> ENDEC = EndecTomfoolery.lazy(() -> (StructEndec<Fragment>) Endec.dispatchedStruct(
                    FragmentType::endec,
                    Fragment::type,
                    Endec.<FragmentType<?>>ifAttr(EndecTomfoolery.UBER_COMPACT_ATTRIBUTE, Endec.INT.xmap(FragmentType::getFromInt, FragmentType::getIntId))
                            .orElse(Identifier.ENDEC.xmap(FragmentType.REGISTRY::get, FragmentType::getID))
            )
    );



    FragmentType<?> type();
}
