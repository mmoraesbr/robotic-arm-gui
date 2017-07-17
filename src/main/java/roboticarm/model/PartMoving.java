package roboticarm.model;

import lombok.Builder;
import lombok.Getter;

/**
 * Created by mmoraes on 18/06/17.
 */
@Builder
@Getter
public class PartMoving {
    Part part;
    Integer position;
    Integer speed;
    boolean blocking = true;

    public PartPosition getPartPosition() {
        return PartPosition.builder().position(position).part(part).build();
    }

    public boolean isValid() {
        return part.accept(position);
    }
}
