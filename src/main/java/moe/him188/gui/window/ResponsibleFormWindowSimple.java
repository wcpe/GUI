package moe.him188.gui.window;

import cn.nukkit.Player;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.response.FormResponseSimple;
import cn.nukkit.form.window.FormWindowSimple;
import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import moe.him188.gui.element.ResponsibleButton;
import moe.him188.gui.window.listener.SimpleResponseListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 能直接接受提交表单 ({@link #onClicked(BiConsumer)} 和关闭窗口事件({@link #onClosed(Consumer)}) 的 {@link FormWindowSimple}.
 * 相较于 {@link FormWindowSimple}, 该类无需依赖于 {@link Listener} 去监听 {@link PlayerFormRespondedEvent}, 而可以直接通过 lambda 方法收到返回数据.
 * 这个表单的组成是: 一段文字说明 + 很多个按钮. 按钮之间不能插入另一段文字说明.
 *
 * @author Him188moe @ ProjectARK Project
 */
public class ResponsibleFormWindowSimple extends FormWindowSimple {
    @Expose(serialize = false, deserialize = false)
    private BiConsumer<Integer, Player> buttonClickedListener = null;

    @Expose(serialize = false, deserialize = false)
    private Consumer<Player> windowClosedListener = null;

    public ResponsibleFormWindowSimple(String title, String content) {
        this(title, "\n" + content + "\n", new ArrayList<>());
    }

    public ResponsibleFormWindowSimple(String title, String content, List<ElementButton> buttons) {
        super(Objects.requireNonNull(title), "\n" + Objects.requireNonNull(content) + "\n", Objects.requireNonNull(buttons));
    }


    /**
     * 在玩家提交表单
     *
     * @param listener 调用的方法
     */
    public ResponsibleFormWindowSimple onClicked(BiConsumer<Integer, Player> listener) {
        this.buttonClickedListener = listener;
        return this;
    }

    /**
     * 在玩家提交表单后调用
     *
     * @param listener 调用的方法(无 Player)
     */
    public ResponsibleFormWindowSimple onClicked(Consumer<Integer> listener) {
        Objects.requireNonNull(listener);
        this.buttonClickedListener = (id, player) -> listener.accept(id);
        return this;
    }

    /**
     * 在玩家提交表单后调用
     *
     * @param listener 调用的方法(无参数)
     */
    public ResponsibleFormWindowSimple onClicked(Runnable listener) {
        Objects.requireNonNull(listener);
        this.buttonClickedListener = (id, player) -> listener.run();
        return this;
    }


    /**
     * 在玩家关闭窗口而没有点击按钮提交表单后调用.
     *
     * @param listener 调用的方法
     */
    public ResponsibleFormWindowSimple onClosed(Consumer<Player> listener) {
        this.windowClosedListener = listener;
        return this;
    }

    /**
     * 在玩家关闭窗口而没有点击按钮提交表单后调用.
     *
     * @param listener 调用的方法
     */
    public ResponsibleFormWindowSimple onClosed(Runnable listener) {
        Objects.requireNonNull(listener);
        this.windowClosedListener = (player) -> listener.run();
        return this;
    }

    @Override
    public String getJSONData() {
        return new Gson().toJson(this, FormWindowSimple.class);
    }

    public void callClicked(int id, Player player) {
        Objects.requireNonNull(player);
        ElementButton button = getButtons().get(id);
        if (button instanceof ResponsibleButton) {
            ((ResponsibleButton) button).callClicked(player);
        }
        if (this.buttonClickedListener != null) {
            this.buttonClickedListener.accept(id, player);
        }
    }

    public void callClosed(Player player) {
        Objects.requireNonNull(player);
        this.windowClosedListener.accept(player);
    }

    public static boolean onEvent(PlayerFormRespondedEvent event) {
        if (event.getWindow() instanceof ResponsibleFormWindowSimple && event.getResponse() instanceof FormResponseSimple) {
            ResponsibleFormWindowSimple window = (ResponsibleFormWindowSimple) event.getWindow();
            if (window instanceof SimpleResponseListener) {
                ((SimpleResponseListener) window).onClicked((FormResponseSimple) event.getResponse(), event.getPlayer());
            }

            if (event.getWindow().wasClosed() || event.getResponse() == null) {
                if (window.windowClosedListener == null) {
                    return true;
                }
                window.callClosed(event.getPlayer());
            } else {
                window.callClicked(((FormResponseSimple) event.getResponse()).getClickedButtonId(), event.getPlayer());
            }
            return true;
        }
        return false;
    }
}