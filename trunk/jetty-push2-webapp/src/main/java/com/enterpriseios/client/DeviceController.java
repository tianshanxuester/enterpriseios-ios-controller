package com.enterpriseios.client;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.HasKeyboardPagingPolicy;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.*;
import com.enterpriseios.client.policies.iPadPolicyDialogBox;
import com.enterpriseios.client.policies.iPhonePolicyDialogBox;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: inventit
 * Date: 2010/11/26
 * Time: 11:33:25
 * To change this template use File | Settings | File Templates.
 */
public class DeviceController extends Composite
{

    @UiTemplate("DeviceController.ui.xml")
    interface Binder extends UiBinder<HTMLPanel, DeviceController>
    {
    }

    @UiField(provided=true)
    ShowMorePagerPanel pagerPanel;

    @UiField
    RangeLabelPager rangeLabelPager;

    @UiField
    TextBox user;

    @UiField
    TextBox password;

    @UiField
    Button add;
    
    @UiField
    Button remove;

    private AccountServiceAsync accountService = GWT.create(AccountService.class);
    private DeviceMonitoringServiceAsync deviceService = GWT.create(DeviceMonitoringService.class);
    private MultiSelectCellList<Account> cellList;
    private ListDataProvider<Account> dataProvider = new ListDataProvider<Account>();
    private Account selected;
    private AccountRemovalDialogBox confirmRemove;
    private Timer timer;
    private static Logger logger = Logger.getLogger("DeviceController");
    private MultiSelectionModel selectionModel;

    public DeviceController()
    {
        AccountCell accountCell=new AccountCell();
        ProvidesKey keyProvider=new ProvidesKey<Account>()
                    {
                        public Object getKey(Account account)
                        {
                            return account == null ? null : account.getUser();
                        }
                    };
        selectionModel=new MultiSelectionModel(keyProvider);
        cellList=new MultiSelectCellList<Account>(accountCell,selectionModel);
        cellList.setPageSize(30);
        cellList.setKeyboardPagingPolicy(HasKeyboardPagingPolicy.KeyboardPagingPolicy.INCREASE_RANGE);
        pagerPanel=new ShowMorePagerPanel();
        pagerPanel.setDisplay(cellList);
        confirmRemove=new AccountRemovalDialogBox(this,false,true);

        final iPhonePolicyDialogBox iPhone=new iPhonePolicyDialogBox(this,false,true);
        iPhone.setAnimationEnabled(true);

        final iPadPolicyDialogBox iPad=new iPadPolicyDialogBox(DeviceController.this,false,true);
        iPad.setAnimationEnabled(true);

        Binder uiBinder = GWT.create(Binder.class);
        initWidget(uiBinder.createAndBindUi(this));
        
        cellList.addSelectionChangeHandler(
                new SelectionChangeEvent.Handler()
                {
                    public void onSelectionChange(SelectionChangeEvent event)
                    {
                        Set<Account> set=cellList.getSelectedSet();

                        selected=set.iterator().next();
                        if(selected.getDeviceId()!=null)
                        {
                            AsyncCallback<Device> callback=
                                new AsyncCallback<Device>()
                                {

                                    public void onFailure(Throwable throwable)
                                    {
                                        throwable.printStackTrace();
                                    }

                                    public void onSuccess(Device device)
                                    {
                                        String type=device.getType();
                                        if(type!=null)
                                        {
                                            if(type.equals("iPhone")||type.equals("iPod"))
                                            {
                                                iPhone.setDevice(device,false);
                                                iPhone.center();
                                                iPhone.show();
                                            }
                                            else if(type.equals("iPad"))
                                            {
                                                iPad.setDevice(device,false);
                                                iPad.center();
                                                iPad.show();
                                            }
                                        }

                                    }
                                };
                            deviceService.getDeviceById(selected.getDeviceId(),callback);
                            timer.cancel();
                            logger.info("Timer Canceled");
                        }
                        
                    }
                });

        dataProvider.addDataDisplay(cellList);
        rangeLabelPager.setDisplay(cellList);
        updateList();
        updateList(5000);
    }

    public Account getSelectedAccount()
    {
        return selected;
    }

    public void deselectSelected()
    {
        selectionModel.setSelected(selected,false);
        selected=null;
        updateList();
    }

    public void disassociateDevice()
    {
        AsyncCallback<Void> callback=
                new AsyncCallback<Void>()
                {

                    public void onFailure(Throwable throwable)
                    {
                        throwable.printStackTrace();
                    }

                    public void onSuccess(Void devices)
                    {
                        updateList();
                    }
                };
        logger.info("disassociateDevice:"+selected.toString());
        accountService.disassociateDevice(selected,callback);        
    }

    public DeviceMonitoringServiceAsync getDeviceMonitoringService()
    {
        return deviceService;
    }

    public AccountServiceAsync getAccountService()
    {
        return accountService;
    }

    public void updateList(int schedule)
    {
        timer=new Timer(){

            @Override
            public void run()
            {
                AsyncCallback<List<Account>> callback=
                new AsyncCallback<List<Account>>()
                {

                    public void onFailure(Throwable throwable)
                    {
                        throwable.printStackTrace();
                    }

                    public void onSuccess(List<Account> devices)
                    {
                        List<Account> current=dataProvider.getList();
                        current.clear();
                        current.addAll(devices);
                        dataProvider.refresh();
                    }
                };
                accountService.getAllAccount(callback);
            }
        };
        timer.scheduleRepeating(schedule);

    }

    private void updateList()
    {
        AsyncCallback<List<Account>> callback=
        new AsyncCallback<List<Account>>()
        {

            public void onFailure(Throwable throwable)
            {
                throwable.printStackTrace();
            }

            public void onSuccess(List<Account> devices)
            {
                List<Account> current=dataProvider.getList();
                current.clear();
                current.addAll(devices);
                dataProvider.refresh();
            }
        };
        accountService.getAllAccount(callback);
    }

    @UiHandler("add")
    public void addHandler(ClickEvent event)
    {
        AsyncCallback<Void> callback=
            new AsyncCallback<Void>()
            {

                public void onFailure(Throwable throwable)
                {
                    throwable.printStackTrace();
                }

                public void onSuccess(Void devices)
                {
                    updateList();
                    user.setValue(null);
                    password.setValue(null);
                }
            };
        String vUser=user.getValue();
        String vPassword=password.getValue();

        if(vUser!=null && !vUser.isEmpty() &&
                vPassword!=null && !vPassword.isEmpty())
        accountService.add(new Account(vUser,vPassword),callback);
    }

    @UiHandler("remove")
    public void removeHandler(ClickEvent event)
    {
        confirmRemove.setUser(selected.getUser());
        confirmRemove.center();
        confirmRemove.show();
    }

    static class AccountCell extends AbstractCell<Account>
    {

        public AccountCell(){}


        @Override
        public void render(Account value, Object key, SafeHtmlBuilder sb)
        {
            if (value==null)
                return;
            sb.appendHtmlConstant("<table>");
            sb.appendHtmlConstant("<tr><td style='font-size:95%;'>");
            sb.appendEscaped(value.getUser() +"/"+value.getPassword() + (value.getDeviceId()!=null ? "" : "[No associated device]"));
            sb.appendHtmlConstant("</td></tr>");
            sb.appendHtmlConstant("</table>");
        }
    }

    static class MultiSelectCellList<T> extends CellList<T>
    {

        private final MultiSelectionModel<T> mSelectionModel;

        public void addSelectionChangeHandler(SelectionChangeEvent.Handler handler)
        {
            mSelectionModel.addSelectionChangeHandler(handler);
        }

        public MultiSelectCellList(Cell<T> cell,MultiSelectionModel<T> selectionModel)
        {
            super(cell);
            mSelectionModel = selectionModel;
            setSelectionModel(mSelectionModel);
        }

        @Override
        protected void doSelection(Event event, T value, int indexOnPage)
        {
            boolean shift = false;
            String sEventType = event.getType();
            if("click".equals(sEventType) ||
                "mousedown".equals(sEventType) ||
                "mouseup".equals(sEventType) )
                shift = event.getShiftKey();

            if (mSelectionModel != null)
            {

                boolean bSelected = mSelectionModel.isSelected(value);
                if(!shift)
                {
                    // deselect all other
                    for(T ref : mSelectionModel.getSelectedSet())
                        mSelectionModel.setSelected(ref,false);

                    mSelectionModel.setSelected(value,true);
                    return;
                }
                else
                    mSelectionModel.setSelected(value, !bSelected);
            }
        }

        public int getSelectedCount()
        {
            return mSelectionModel.getSelectedSet().size();
        }

        public java.util.Set<T> getSelectedSet()
        {
            return mSelectionModel.getSelectedSet();
        }

    }
}