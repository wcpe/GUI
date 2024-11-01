package moe.him188.gui.window;

import cn.nukkit.Player;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.response.FormResponse;
import cn.nukkit.form.response.FormResponseSimple;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.window.FormWindowSimple;
import com.google.gson.Gson;
import moe.him188.gui.element.ResponsibleButton;
import moe.him188.gui.utils.Backable;
import moe.him188.gui.window.listener.action.ClickListenerSimple;
import moe.him188.gui.window.listener.response.ResponseListenerSimple;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 能直接接受提交表单 ({@link #onClicked} 和关闭窗口事件({@link #onClosed}) 的 {@link FormWindowSimple}.
 * 相较于 {@link FormWindowSimple}, 该类无需依赖于 {@link Listener} 去监听 {@link PlayerFormRespondedEvent}, 而可以直接通过 lambda 方法收到返回数据.
 * 这个表单的组成是: 一段文字说明 + 很多个按钮. 按钮之间不能插入另一段文字说明.
 * <br>
 * {@link FormWindowSimple} that can receive click button event({@link #onClicked}) and close window event({@link #onClosed}).
 * Comparing with {@link FormWindowSimple}, this responsible one does not need {@link Listener} to listen {@link PlayerFormRespondedEvent},
 * but it can directly receive {@link FormResponse} through lambda statements.
 * The composition of the form is: A piece of message + button(s). There are no more messages between buttons.
 *
 * @author Him188moe @ GUI Project
 */
public class ResponsibleFormWindowSimple extends FormWindowSimple implements Backable, ResponseListenerSimple {
    protected transient BiConsumer<Integer, Player> buttonClickedListener = null;

    protected transient Consumer<Player> windowClosedListener = null;

    private transient FormWindow parent;

    public ResponsibleFormWindowSimple() {
        this("", "", new ArrayList<>());
    }

    public ResponsibleFormWindowSimple(String content) {
        this("", content, new ArrayList<>());
    }

    public ResponsibleFormWindowSimple(String title, String content) {
        this(title, content, new ArrayList<>());
    }

    public ResponsibleFormWindowSimple(String title, String content, String... buttons) {
        super(title, content, Arrays.stream(buttons).map(ElementButton::new).collect(Collectors.toList()));
    }

    public ResponsibleFormWindowSimple(String title, String content, ElementButton... buttons) {
        super(title, content, Arrays.asList(buttons));
    }

    public ResponsibleFormWindowSimple(String title, String content, @NotNull List<ElementButton> buttons) {
        super(Objects.requireNonNull(title), Objects.requireNonNull(content), Objects.requireNonNull(buttons));
    }

    @Override
    public void setParent(FormWindow parent) {
        this.parent = parent;
    }

    @Override
    public FormWindow getParent() {
        return parent;
    }

    /**
     * 在玩家提交表单后调用 <br>
     * Called on submitted
     *
     * @param listener 调用的方法
     */
    public final ResponsibleFormWindowSimple onClicked(@NotNull BiConsumer<Integer, Player> listener) {
        Objects.requireNonNull(listener);
        this.buttonClickedListener = listener;
        return this;
    }

    /**
     * 在玩家提交表单后调用 <br>
     * Called on submitted
     *
     * @param listener 调用的方法(无 Player)
     */
    public final ResponsibleFormWindowSimple onClicked(@NotNull Consumer<Integer> listener) {
        Objects.requireNonNull(listener);
        this.buttonClickedListener = (id, player) -> listener.accept(id);
        return this;
    }

    /**
     * 在玩家提交表单后调用 <br>
     * Called on submitted
     *
     * @param listener 调用的方法(无参数)
     */
    public final ResponsibleFormWindowSimple onClicked(@NotNull Runnable listener) {
        Objects.requireNonNull(listener);
        this.buttonClickedListener = (id, player) -> listener.run();
        return this;
    }


    /**
     * 在玩家关闭窗口而没有点击按钮提交表单后调用. <br>
     * Called on submitted
     *
     * @param listener 调用的方法
     */
    public final ResponsibleFormWindowSimple onClosed(@NotNull Consumer<Player> listener) {
        Objects.requireNonNull(listener);
        this.windowClosedListener = listener;
        return this;
    }

    /**
     * 在玩家关闭窗口而没有点击按钮提交表单后调用. <br>
     * Called on closed without submitting.
     *
     * @param listener 调用的方法
     */
    public final ResponsibleFormWindowSimple onClosed(@NotNull Runnable listener) {
        Objects.requireNonNull(listener);
        this.windowClosedListener = (player) -> listener.run();
        return this;
    }

    /**
     * 快速添加 {@link ElementButton}. <br>
     * Fast adding {@link ElementButton}
     *
     * @param name button name
     */
    public void addButton(String name) {
        super.addButton(new ElementButton(name));
    }


    /**
     * 快速添加 {@link ResponsibleButton}. <br>
     * Fast adding {@link ResponsibleButton}
     *
     * @param name          button name
     * @param clickListener listener
     */
    public void addButton(String name, @NotNull ClickListenerSimple clickListener) {
        super.addButton(new ResponsibleButton(name, clickListener));
    }

    /**
     * 快速添加 {@link ResponsibleButton}. <br>
     * Fast adding {@link ResponsibleButton}
     *
     * @param name          button name
     * @param clickListener listener
     */
    public void addButton(String name, @NotNull Consumer<Player> clickListener) {
        super.addButton(new ResponsibleButton(name, clickListener));
    }

    @Override
    public String getJSONData() {
        return new Gson().toJson(this, FormWindowSimple.class);
    }

    public void callClicked(int id, @NotNull Player player) {
        Objects.requireNonNull(player);

        ElementButton button = getButtons().get(id);
        if (button instanceof ResponsibleButton) {
            ((ResponsibleButton) button).callClicked(player);
        }

        this.onClicked(id, player);

        if (this.buttonClickedListener != null) {
            this.buttonClickedListener.accept(id, player);
        }
    }

    public void callClosed(Player player) {
        Objects.requireNonNull(player);

        this.onClosed(player);

        if (this.windowClosedListener != null) {
            this.windowClosedListener.accept(player);
        }
    }

    static boolean onEvent(FormWindow formWindow, FormResponse response, Player player) {
        if (formWindow instanceof ResponsibleFormWindowSimple) {
            ResponsibleFormWindowSimple window = (ResponsibleFormWindowSimple) formWindow;

            if (window.wasClosed() || response == null) {
                window.callClosed(player);
                window.closed = false;//for resending
            } else {
                window.callClicked(((FormResponseSimple) response).getClickedButtonId(), player);
            }
            return true;
        }
        return false;
    }
}