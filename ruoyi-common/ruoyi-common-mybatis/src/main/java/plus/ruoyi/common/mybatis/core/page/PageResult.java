package plus.ruoyi.common.mybatis.core.page;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;
import plus.ruoyi.common.core.utils.MapstructUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页结果数据对象
 *
 * @param <T> 数据类型
 * @author 抓蛙师
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class PageResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 数据记录列表
     */
    private List<T> records;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 当前页码
     */
    private long current;

    /**
     * 每页大小
     */
    private long size;

    /**
     * 是否为最后一页
     */
    private boolean last;

    /**
     * 构造简单分页结果
     *
     * @param records 数据记录列表
     * @param total   总记录数
     */
    public PageResult(List<T> records, long total) {
        this.records = records;
        this.total = total;
        this.current = 1;
        this.size = records.size();
        this.last = true;
    }

    /**
     * 构建空的分页结果
     *
     * @param <T> 数据类型
     * @return 空的分页结果
     */
    public static <T> PageResult<T> of() {
        return new PageResult<>(Collections.emptyList(), 0);
    }

    /**
     * 从 MyBatis-Plus 的 IPage 构建分页结果
     *
     * @param page MyBatis-Plus 分页对象
     * @param <T>  数据类型
     * @return 分页结果
     */
    public static <T> PageResult<T> of(IPage<T> page) {
        // 手动计算是否为最后一页
        boolean last = page.getCurrent() >= page.getPages() || page.getPages() == 0;

        return new PageResult<>(
            page.getRecords(),
            page.getTotal(),
            page.getCurrent(),
            page.getSize(),
            last
        );
    }

    /**
     * 从 MyBatis-Plus 的 Page 构建分页结果
     *
     * @param page MyBatis-Plus 分页对象
     * @param <T>  数据类型
     * @return 分页结果
     */
    public static <T> PageResult<T> of(Page<T> page) {
        return new PageResult<>(
            page.getRecords(),
            page.getTotal(),
            page.getCurrent(),
            page.getSize(),
            !page.hasNext()
        );
    }

    /**
     * 从数据列表构建分页结果（不分页）
     *
     * @param records 数据记录列表
     * @param <T>     数据类型
     * @return 分页结果
     */
    public static <T> PageResult<T> of(List<T> records) {
        return new PageResult<>(records, records.size());
    }

    /**
     * 手动分页构建分页结果
     *
     * @param records 完整数据记录列表
     * @param current 当前页码（从1开始）
     * @param size    每页大小
     * @param <T>     数据类型
     * @return 分页结果
     */
    public static <T> PageResult<T> of(List<T> records, long current, long size) {
        if (records == null || records.isEmpty()) {
            return of();
        }

        long total = records.size();
        long start = (current - 1) * size;
        long end = Math.min(start + size, total);

        List<T> pageData = (start >= total) ?
            Collections.emptyList() :
            records.subList((int) start, (int) end);

        return new PageResult<>(
            pageData,
            total,
            current,
            size,
            end >= total
        );
    }

    /**
     * 转换数据类型
     *
     * @param targetClass 目标类型
     * @param <R>         目标数据类型
     * @return 转换后的分页结果
     */
    public <R> PageResult<R> convert(Class<R> targetClass) {
        List<R> convertedRecords = MapstructUtils.convert(this.records, targetClass);
        return new PageResult<>(convertedRecords, this.total, this.current, this.size, this.last);
    }

    /**
     * 使用自定义转换器转换数据类型
     *
     * @param converter 转换器函数
     * @param <R>       目标数据类型
     * @return 转换后的分页结果
     */
    public <R> PageResult<R> map(java.util.function.Function<T, R> converter) {
        List<R> convertedRecords = this.records.stream()
            .map(converter)
            .toList();
        return new PageResult<>(convertedRecords, this.total, this.current, this.size, this.last);
    }

    /**
     * 获取总页数
     *
     * @return 总页数
     */
    public long getPages() {
        return size == 0 ? 0 : (total + size - 1) / size;
    }

    /**
     * 是否有上一页
     *
     * @return 是否有上一页
     */
    public boolean hasPrevious() {
        return current > 1;
    }

    /**
     * 是否有下一页
     *
     * @return 是否有下一页
     */
    public boolean hasNext() {
        return !last;
    }

}
