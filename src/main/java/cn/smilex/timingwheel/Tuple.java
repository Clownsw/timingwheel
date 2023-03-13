package cn.smilex.timingwheel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author smilex
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tuple<LEFT, RIGHT> {
    private LEFT left;
    private RIGHT right;
}
