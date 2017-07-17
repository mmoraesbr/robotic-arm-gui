package roboticarm.model;

import lombok.Builder;
import lombok.Getter;

/**
 * Created by mmoraes on 18/06/17.
 */
@Builder
@Getter
public class PartPosition {
    Part part;
    int position;
}
