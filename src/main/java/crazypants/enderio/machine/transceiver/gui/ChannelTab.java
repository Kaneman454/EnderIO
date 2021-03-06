package crazypants.enderio.machine.transceiver.gui;

import java.awt.Color;

import crazypants.util.PlayerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import crazypants.enderio.gui.ITabPanel;
import crazypants.enderio.gui.IconButtonEIO;
import crazypants.enderio.gui.IconEIO;
import crazypants.enderio.gui.ToggleButtonEIO;
import crazypants.enderio.machine.transceiver.Channel;
import crazypants.enderio.machine.transceiver.ChannelType;
import crazypants.enderio.machine.transceiver.ClientChannelRegister;
import crazypants.enderio.machine.transceiver.PacketAddRemoveChannel;
import crazypants.enderio.machine.transceiver.PacketSendRecieveChannel;
import crazypants.enderio.machine.transceiver.TileTransceiver;
import crazypants.enderio.network.PacketHandler;
import crazypants.gui.GuiScrollableList;
import crazypants.gui.ListSelectionListener;
import crazypants.render.ColorUtil;
import crazypants.util.Lang;

public class ChannelTab implements ITabPanel {

  protected static final int ADD_BUTTON_ID = 3;
  protected static final int PRIVATE_BUTTON_ID = 4;
  private static final int DELETE_CHANNEL_BUTTON_ID = 5;
  
  private static final int SEND_BUTTON_ID = 6;
  private static final int RECIEVE_BUTTON_ID = 7;

  ChannelType type;
  GuiTransceiver parent;

  IconButtonEIO addButton;
  ToggleButtonEIO privateButton;

  GuiTextField newChannelTF;
  GuiChannelList channelList;
  
  GuiChannelList sendChannels;
  GuiChannelList recieveChannels;
  
  IconButtonEIO deleteChannelB;
  IconButtonEIO sendB;
  IconButtonEIO recieveB;
  
  ListSelectionListener<Channel> selectionListener;
  TileTransceiver transceiver;

  public ChannelTab(GuiTransceiver guiTransceiver, ChannelType type) {
    parent = guiTransceiver;
    this.type = type;
    transceiver = guiTransceiver.getTransciever();

    newChannelTF = new GuiTextField(parent.getFontRenderer(), 7, 12, 103, 16);    
    addButton = new IconButtonEIO(parent, ADD_BUTTON_ID, 137, 12, IconEIO.PLUS);
    addButton.setToolTip(Lang.localize("gui.trans.addChannel"));
    addButton.enabled = false;

    privateButton = new ToggleButtonEIO(parent, PRIVATE_BUTTON_ID, 118, 12, IconEIO.PUBLIC, IconEIO.PRIVATE);
    privateButton.setSelectedToolTip(Lang.localize("gui.trans.privateChannel"));
    privateButton.setUnselectedToolTip(Lang.localize("gui.trans.publicChannel"));

    int w = 104;
    int h = 90;
    int x = 7;
    int y = 48;
    channelList = new GuiChannelList(parent, w, h, x, y);
    channelList.setChannels(ClientChannelRegister.instance.getChannelsForType(type));
    channelList.setShowSelectionBox(true);
    channelList.setScrollButtonIds(100, 101);    
    
    deleteChannelB = new IconButtonEIO(parent, DELETE_CHANNEL_BUTTON_ID, x + w - 20, y + h + 4, IconEIO.MINUS);
    deleteChannelB.setToolTip(Lang.localize("gui.trans.deleteChannel"));
    
    
    x += w + 32;
    h = 35;
    sendChannels = new GuiChannelList(parent, w, h, x, y);
    sendChannels.setChannels(transceiver.getSendChannels(type));
    sendChannels.setShowSelectionBox(true);
    sendChannels.setScrollButtonIds(200, 201);
    
    sendB = new IconButtonEIO(parent,SEND_BUTTON_ID,x -24, y + h/2 - 9, IconEIO.ARROWS);
    
    y += h + 20;
    recieveChannels = new GuiChannelList(parent, w, h, x, y);
    recieveChannels.setChannels(transceiver.getRecieveChannels(type));
    recieveChannels.setShowSelectionBox(true);
    recieveChannels.setScrollButtonIds(300, 301);
        
    recieveB = new IconButtonEIO(parent,RECIEVE_BUTTON_ID,x -24, y + h/2 - 9, IconEIO.ARROWS);
    
    
    selectionListener = new ListSelectionListener<Channel>() {
      
      @Override
      public void selectionChanged(GuiScrollableList<Channel> list, int selectedIndex) {
        if(selectedIndex < 0) {
          return;
        }
        if(list != channelList) {
          channelList.setSelection(-1);
        }
        if(list != sendChannels) {
          sendChannels.setSelection(-1);
        }
        if(list != recieveChannels) {
          recieveChannels.setSelection(-1);
        }
      }
    };
    channelList.addSelectionListener(selectionListener);
    sendChannels.addSelectionListener(selectionListener);
    recieveChannels.addSelectionListener(selectionListener);
        
  }

  @Override
  public void onGuiInit(int x, int y, int width, int height) {
    addButton.onGuiInit();
    privateButton.onGuiInit();
    deleteChannelB.onGuiInit();
    sendB.onGuiInit();
    recieveB.onGuiInit();

    y = parent.getGuiTop() + 12;
    x = parent.getGuiLeft() + 8;
    newChannelTF.xPosition = x;
    newChannelTF.yPosition = y;
    newChannelTF.setCanLoseFocus(false);
    newChannelTF.setMaxStringLength(32);
    newChannelTF.setFocused(true);

    channelList.onGuiInit(parent);
    sendChannels.onGuiInit(parent);
    recieveChannels.onGuiInit(parent);
  }

  @Override
  public void deactivate() {
    addButton.detach();
    privateButton.detach();
    deleteChannelB.detach();
    sendB.detach();
    recieveB.detach();
  }

  @Override
  public void keyTyped(char par1, int par2) {    
    newChannelTF.textboxKeyTyped(par1, par2);    
    addButton.enabled = newChannelTF.getText().trim().length() > 0;
  }

  @Override
  public IconEIO getIcon() {
    switch (type) {
    case FLUID:
      return IconEIO.WRENCH_OVERLAY_FLUID;
    case ITEM:
      return IconEIO.WRENCH_OVERLAY_ITEM;
    case POWER:
      return IconEIO.WRENCH_OVERLAY_POWER;   
    case RAIL:
      return IconEIO.ENDER_RAIL;
    default:
      return IconEIO.WRENCH_OVERLAY_POWER;
    }
  }

  @Override
  public void updateScreen() {
    newChannelTF.updateCursorCounter();
  }

  @Override
  public void render(float partialTick, int mouseX, int mouseY) {
    newChannelTF.drawTextBox();
    channelList.drawScreen(mouseX, mouseY, partialTick);
    sendChannels.drawScreen(mouseX, mouseY, partialTick);
    recieveChannels.drawScreen(mouseX, mouseY, partialTick);
    
    int left = parent.getGuiLeft();
    int top = parent.getGuiTop();
    int x = left + 59;
    int y = top + 36;
    parent.drawCenteredString(parent.getFontRenderer(), Lang.localize("gui.available"), x, y, ColorUtil.getRGB(Color.white));
    
    x = left + 199;
    parent.drawCenteredString(parent.getFontRenderer(), Lang.localize("gui.send"), x, y, ColorUtil.getRGB(Color.white));
    
    y += 56;
    parent.drawCenteredString(parent.getFontRenderer(), Lang.localize("gui.receive"), x, y, ColorUtil.getRGB(Color.white));           
  }

  @Override
  public void actionPerformed(GuiButton guiButton) {
    if(guiButton.id == ADD_BUTTON_ID) {
      addChannelPressed();      
    } else if(guiButton.id == DELETE_CHANNEL_BUTTON_ID) {
      deleteChannelPressed();      
    } else if(guiButton.id == SEND_BUTTON_ID) {
      sendTogglePressed();      
    } else if(guiButton.id == RECIEVE_BUTTON_ID) {
      receiveTogglePressed();      
    }
  }

  protected void receiveTogglePressed() {
    Channel c = channelList.getSelectedElement();
    if(c != null && !transceiver.getRecieveChannels(type).contains(c)) {
      transceiver.addRecieveChanel(c);
      PacketHandler.INSTANCE.sendToServer(new PacketSendRecieveChannel(transceiver, false, true, c));
    } else {
      c = recieveChannels.getSelectedElement();
      if(c != null) {
        transceiver.removeRecieveChanel(c);
        PacketHandler.INSTANCE.sendToServer(new PacketSendRecieveChannel(transceiver, false, false, c));
      }
    }
  }

  protected void sendTogglePressed() {
    Channel c = channelList.getSelectedElement();
    if(c != null && !transceiver.getSendChannels(type).contains(c)) {
      transceiver.addSendChanel(c);
      PacketHandler.INSTANCE.sendToServer(new PacketSendRecieveChannel(transceiver, true, true, c));
    } else {
      c = sendChannels.getSelectedElement();
      if(c != null) {
        transceiver.removeSendChanel(c);
        PacketHandler.INSTANCE.sendToServer(new PacketSendRecieveChannel(transceiver, true, false, c));
      }
    }
  }

  private void deleteChannelPressed() {
    Channel c = channelList.getSelectedElement();
    if(c != null) {        
      ClientChannelRegister.instance.removeChannel(c);
      PacketHandler.INSTANCE.sendToServer(new PacketAddRemoveChannel(c, false));    
    }
  }

  private void addChannelPressed() {
    if(newChannelTF.getText() == null || newChannelTF.getText().trim().isEmpty()) {
      return;
    }
    Channel c;
    if(privateButton.isSelected()) {
      c = new Channel(newChannelTF.getText(), PlayerUtil.getPlayerUUID(Minecraft.getMinecraft().thePlayer.getGameProfile().getName()), type);
    } else {
      c = new Channel(newChannelTF.getText(), null, type);
    }
    ClientChannelRegister.instance.addChannel(c);
    PacketHandler.INSTANCE.sendToServer(new PacketAddRemoveChannel(c, true));           
    channelList.setSelection(c);
    newChannelTF.setText("");
  }

  @Override
  public void mouseClicked(int x, int y, int par3) {
    newChannelTF.mouseClicked(x, y, par3);
  }

}
