package games.rednblack.editor.view.ui.properties;

import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.MsgAPI;
import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.mediator.Mediator;

public abstract class UIAbstractPropertiesMediator<T, V extends UIAbstractProperties> extends Mediator<V> {
    private Sandbox sandbox;

    protected T observableReference;

    protected boolean lockUpdates = true;

    public UIAbstractPropertiesMediator(String mediatorName, V viewComponent) {
        super(mediatorName, viewComponent);

        sandbox = Sandbox.getInstance();
        facade = HyperLap2DFacade.getInstance();
    }

    @Override
    public void onRegister() {
        facade = HyperLap2DFacade.getInstance();
    }


    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                MsgAPI.ITEM_DATA_UPDATED,
                viewComponent.getUpdateEventName()
        };
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);


        if(notification.getName().equals(viewComponent.getUpdateEventName())) {
            if(!lockUpdates) {
                translateViewToItemData();
            }
        }

        switch (notification.getName()) {
            case MsgAPI.ITEM_DATA_UPDATED:
                onItemDataUpdate();
                break;
            default:
                break;
        }
    }

    public void setItem(T item) {
        observableReference = item;
        lockUpdates = true;
        translateObservableDataToView(observableReference);
        lockUpdates = false;
    }

    public void onItemDataUpdate() {
        lockUpdates = true;
        translateObservableDataToView(observableReference);
        lockUpdates = false;
    }

    protected abstract void translateObservableDataToView(T item);

    protected abstract void translateViewToItemData();
}