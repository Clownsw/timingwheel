package cn.smilex.timingwheel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 元组
 *
 * @author yanglujia
 * @date 2024/1/22/15:04
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tuple<LEFT, RIGHT> {
    /* left item */
    private LEFT left;
    /* right item */
    private RIGHT right;
}
