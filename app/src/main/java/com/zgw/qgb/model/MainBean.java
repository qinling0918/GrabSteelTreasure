package com.zgw.qgb.model;

import java.util.List;

/**
 * Created by Tsinling on 2017/10/17 11:16.
 * description:
 */

public class MainBean {
    private List<NoticelistBean> noticelist;

    public List<NoticelistBean> getNoticelist() {
        return noticelist;
    }

    public void setNoticelist(List<NoticelistBean> noticelist) {
        this.noticelist = noticelist;
    }

    public static class NoticelistBean {
        /**
         * Id : 9466
         * FormMemberId : 0
         * ToMemberId : 73740
         * TempTitle : 1234
         * TempText : http://www.baidu.com
         * TempResult : {"result":"ok","taskId":"OSS-1010_020316404eccefd46229f308db7cce59","status":"successed_online"}
         * TempDate : /Date(1507623600793)/
         * CreateTime : /Date(1507623602373)/
         * SendId : 0
         * MsgTypeId : 22
         * IsDelete : 0
         * IsRead : 0
         * ReadDate : /Date(-62135596800000)/
         * IsNowSend : 0
         * SendType : 0
         * Url : http://www.baidu.com
         */

        private int Id;
        private int FormMemberId;
        private int ToMemberId;
        private String TempTitle;
        private String TempText;
        private String TempResult;
        private String TempDate;
        private String CreateTime;
        private int SendId;
        private int MsgTypeId;
        private int IsDelete;
        private int IsRead;
        private String ReadDate;
        private int IsNowSend;
        private int SendType;
        private String Url;

        public int getId() {
            return Id;
        }

        public void setId(int Id) {
            this.Id = Id;
        }

        public int getFormMemberId() {
            return FormMemberId;
        }

        public void setFormMemberId(int FormMemberId) {
            this.FormMemberId = FormMemberId;
        }

        public int getToMemberId() {
            return ToMemberId;
        }

        public void setToMemberId(int ToMemberId) {
            this.ToMemberId = ToMemberId;
        }

        public String getTempTitle() {
            return TempTitle;
        }

        public void setTempTitle(String TempTitle) {
            this.TempTitle = TempTitle;
        }

        public String getTempText() {
            return TempText;
        }

        public void setTempText(String TempText) {
            this.TempText = TempText;
        }

        public String getTempResult() {
            return TempResult;
        }

        public void setTempResult(String TempResult) {
            this.TempResult = TempResult;
        }

        public String getTempDate() {
            return TempDate;
        }

        public void setTempDate(String TempDate) {
            this.TempDate = TempDate;
        }

        public String getCreateTime() {
            return CreateTime;
        }

        public void setCreateTime(String CreateTime) {
            this.CreateTime = CreateTime;
        }

        public int getSendId() {
            return SendId;
        }

        public void setSendId(int SendId) {
            this.SendId = SendId;
        }

        public int getMsgTypeId() {
            return MsgTypeId;
        }

        public void setMsgTypeId(int MsgTypeId) {
            this.MsgTypeId = MsgTypeId;
        }

        public int getIsDelete() {
            return IsDelete;
        }

        public void setIsDelete(int IsDelete) {
            this.IsDelete = IsDelete;
        }

        public int getIsRead() {
            return IsRead;
        }

        public void setIsRead(int IsRead) {
            this.IsRead = IsRead;
        }

        public String getReadDate() {
            return ReadDate;
        }

        public void setReadDate(String ReadDate) {
            this.ReadDate = ReadDate;
        }

        public int getIsNowSend() {
            return IsNowSend;
        }

        public void setIsNowSend(int IsNowSend) {
            this.IsNowSend = IsNowSend;
        }

        public int getSendType() {
            return SendType;
        }

        public void setSendType(int SendType) {
            this.SendType = SendType;
        }

        public String getUrl() {
            return Url;
        }

        public void setUrl(String Url) {
            this.Url = Url;
        }
    }
}
