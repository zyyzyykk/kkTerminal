package com.kkbpro.terminal.controller;

import com.kkbpro.terminal.annotation.Log;
import com.kkbpro.terminal.constant.Constant;
import com.kkbpro.terminal.consumer.WebSocketServer;
import com.kkbpro.terminal.enums.ResultCodeEnum;
import com.kkbpro.terminal.pojo.dto.CooperateInfo;
import com.kkbpro.terminal.result.Result;
import com.kkbpro.terminal.utils.AESUtil;
import com.kkbpro.terminal.utils.LogUtil;
import com.kkbpro.terminal.utils.SSHUtil;
import com.kkbpro.terminal.utils.StringUtil;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * 高级功能接口类
 */
@RestController
@RequestMapping(Constant.API_PREFIX + "/advance")
public class AdvanceController {

    public static final String COOPERATE_SECRET_KEY = StringUtil.generateRandomString(16);

    public static final String[] DOCKER_INFO_CMD = new String[] {
            "echo -n \"$(docker ps -a --format \"{{.ID}}@{{.Names}}@{{.Status}}@{{.Image}}@{{.Ports}}\" | paste -sd '$' -)\"",
            "echo -n \"$(docker images --format \"{{.ID}}@{{.Repository}}@{{.Size}}@{{.CreatedAt}}\" | paste -sd '$' -)\"",
            "echo -n \"$(docker network ls --format \"{{.Name}}\" | xargs -I {} docker network inspect {} --format \"{{.Name}}@{{range .IPAM.Config}}{{.Subnet}}{{end}}@{{range .IPAM.Config}}{{.Gateway}}{{end}}@{{.Created}}\" | paste -sd'$' -)\"",
            "echo -n \"$(docker volume ls --format \"{{.Name}}\" | xargs -I {} docker volume inspect {} --format \"{{.Name}}@{{.Mountpoint}}@{{.CreatedAt}}\" | paste -sd'$' -)\"\n"
    };

    public static final String[] DOCKER_DELETE_CMD = new String[] {
            "docker rm -f ",
            "docker rmi -f ",
            "docker network rm ",
            "docker volume rm "
    };

    public static final String[] DOCKER_CONTAINER_CMD = new String[] {
            "docker start ",
            "docker pause ",
            "docker restart ",
            "docker rm -f ",
            "docker inspect "
    };

    /**
     * 生成协作Key
     */
    @Log
    @GetMapping("/cooperate/key")
    public Result cooperateKey(String sshKey, Boolean readOnly, Integer maxHeadCount) throws Exception {
        String errorMsg = "协作Key生成失败";
        String successMsg = "协作Key生成成功";

        SSHClient ssh = SSHUtil.getSSHClient(sshKey);
        if (ssh == null) {
            return Result.error(ResultCodeEnum.SSH_NOT_EXIST.getState(), "连接断开，" + errorMsg);
        }

        WebSocketServer webSocketServer = WebSocketServer.webSocketServerMap.get(sshKey);
        // 已开启协作
        if (webSocketServer.getCooperateInfo() != null) {
            return Result.error(errorMsg);
        }

        String cooperateId = UUID.randomUUID().toString();
        CooperateInfo cooperateInfo = new CooperateInfo();
        cooperateInfo.setId(cooperateId);
        cooperateInfo.setReadOnly(readOnly);
        cooperateInfo.setMaxHeadCount(maxHeadCount);
        webSocketServer.setCooperateInfo(cooperateInfo);

        String key = StringUtil.changeBase64ToStr(AESUtil.encrypt(cooperateId + "^" + sshKey, COOPERATE_SECRET_KEY));

        return Result.success(successMsg, key);
    }

    /**
     * 结束协作
     */
    @Log
    @PostMapping("/cooperate/end")
    public Result cooperateEnd(String sshKey) {
        String errorMsg = "结束协作失败";
        String successMsg = "结束协作成功";

        SSHClient ssh = SSHUtil.getSSHClient(sshKey);
        WebSocketServer webSocketServer = WebSocketServer.webSocketServerMap.get(sshKey);
        if (ssh == null || webSocketServer == null) {
            return Result.error(ResultCodeEnum.SSH_NOT_EXIST.getState(), "连接断开，" + errorMsg);
        }

        webSocketServer.setCooperateInfo(null);
        List<WebSocketServer> slaveSockets = WebSocketServer.cooperateMap.remove(sshKey);
        if (slaveSockets != null) {
            for (WebSocketServer slaveSocket : slaveSockets) {
                IOUtils.closeQuietly(slaveSocket.getSessionSocket());
            }
        }

        return Result.success(successMsg);
    }

    //    完整命令
    //    echo -n "$(uptime | awk -F'load average: ' '{print $2}' | awk '{print $1}' | sed 's/,//')@" && \
    //    echo -n "$(top -bn1 | grep "Cpu(s)" | sed "s/.*, *\([0-9.]*\)%* id.*/\1/" | awk '{print 100 - $1}')@" && \
    //    echo -n "$(nproc)@" && \
    //    echo -n "$(free -m | grep Mem | awk '{print $3"@"$2}')@" && \
    //    echo -n "$(df -BMB / | grep / | awk '{print $3"@"$2}' | sed 's/[A-Za-z]//g')" && \
    //    echo -n "^" && \
    //    echo -n "$(ps -eo pid,user,%cpu,%mem,comm --sort=-%cpu | head -n 5 | awk 'NR>1 {print $1"@"$2"@"$3"@"$4"@"$5}' | tr '\n' '$' | sed 's/\(.*\)\$$/\1/')" && \
    //    echo -n "^" && \
    //    echo -n "$(cat /proc/net/dev | awk 'NR>2 {up+=$10; down+=$2;} END {print "all:@"up"@"down}')" && \
    //    echo -n "$" && \
    //    echo -n "$(cat /proc/net/dev | awk 'NR>2 {print $1"@"$10"@"$2}' | tr -s ' ' '@' | tr '\n' '$' | sed 's/\(.*\)\$/\1/')" && \
    //    echo -n "^" && \
    //    echo -n "$(cat /proc/diskstats | awk '{if ($3 ~ /[^0-9]$/) {read+=$6; write+=$10}} END {print "all@" read"@"write}')" && \
    //    echo -n "$" && \
    //    echo -n "$(cat /proc/diskstats | awk '{print $3"@"$6"@"$10}' | tr -s ' ' '@' | tr '\n' '$' | sed 's/\(.*\)\$/\1/')" && \
    //    echo -n "^" && \
    //    echo -n $(date +%s%3N)
    /**
     * 获取监控状态
     */
    @Log
    @PostMapping("/monitor")
    public Result monitor(String sshKey) {
        String errorMsg = "获取监控状态失败";
        String successMsg = "获取监控状态成功";

        SSHClient ssh = SSHUtil.getSSHClient(sshKey);
        if (ssh == null) {
            return Result.error(ResultCodeEnum.SSH_NOT_EXIST.getState(), "连接断开，" + errorMsg);
        }
        StringBuilder status = new StringBuilder();
        String command = "echo -n \"$(uptime | awk -F'load average: ' '{print $2}' | awk '{print $1}' | sed 's/,//')@\" && \\\n" +
                "echo -n \"$(top -bn1 | grep \"Cpu(s)\" | sed \"s/.*, *\\([0-9.]*\\)%* id.*/\\1/\" | awk '{print 100 - $1}')@\" && \\\n" +
                "echo -n \"$(nproc)@\" && \\\n" +
                "echo -n \"$(free -m | grep Mem | awk '{print $3\"@\"$2}')@\" && \\\n" +
                "echo -n \"$(df -BMB / | grep / | awk '{print $3\"@\"$2}' | sed 's/[A-Za-z]//g')\" && \\\n" +
                "echo -n \"^\" && \\\n" +
                "echo -n \"$(ps -eo pid,user,%cpu,%mem,comm --sort=-%cpu | head -n 5 | awk 'NR>1 {print $1\"@\"$2\"@\"$3\"@\"$4\"@\"$5}' | tr '\\n' '$' | sed 's/\\(.*\\)\\$$/\\1/')\" && \\\n" +
                "echo -n \"^\" && \\\n" +
                "echo -n \"$(cat /proc/net/dev | awk 'NR>2 {up+=$10; down+=$2;} END {print \"all:@\"up\"@\"down}')\" && \\\n" +
                "echo -n \"$\" && \\\n" +
                "echo -n \"$(cat /proc/net/dev | awk 'NR>2 {print $1\"@\"$10\"@\"$2}' | tr -s ' ' '@' | tr '\\n' '$' | sed 's/\\(.*\\)\\$/\\1/')\" && \\\n" +
                "echo -n \"^\" && \\\n" +
                "echo -n \"$(cat /proc/diskstats | awk '{if ($3 ~ /[^0-9]$/) {read+=$6; write+=$10}} END {print \"all@\" read\"@\"write}')\" && \\\n" +
                "echo -n \"$\" && \\\n" +
                "echo -n \"$(cat /proc/diskstats | awk '{print $3\"@\"$6\"@\"$10}' | tr -s ' ' '@' | tr '\\n' '$' | sed 's/\\(.*\\)\\$/\\1/')\" && \\\n" +
                "echo -n \"^\" && \\\n" +
                "echo -n $(date +%s%3N)";
        try {
            int exitStatus = SSHUtil.executeCommand(ssh, command, status);
            if (exitStatus != 0) return Result.error(errorMsg);
        } catch (Exception e) {
            LogUtil.logException(this.getClass(), e);
            return Result.error(errorMsg);
        }

        return Result.success(200, successMsg, status.toString());
    }

    /**
     * 获取Docker版本
     */
    @Log
    @PostMapping("/docker/version")
    public Result dockerVersion(String sshKey) {
        String errorMsg = "获取Docker版本失败";
        String successMsg = "获取Docker版本成功";

        SSHClient ssh = SSHUtil.getSSHClient(sshKey);
        if (ssh == null) {
            return Result.error(ResultCodeEnum.SSH_NOT_EXIST.getState(), "连接断开，" + errorMsg);
        }
        StringBuilder version = new StringBuilder();
        String command = "docker version --format \"{{.Server.Version}}\"";
        try {
            int exitStatus = SSHUtil.executeCommand(ssh, command, version);
            // 1权限不足/127未安装
            if (exitStatus != 0) return Result.error(500, errorMsg, exitStatus);
        } catch (Exception e) {
            LogUtil.logException(this.getClass(), e);
            return Result.error(errorMsg);
        }

        return Result.success(200, successMsg, version.toString());
    }

    // 完整命令
    //    echo -n "$(docker ps -a --format "{{.ID}}@{{.Names}}@{{.Status}}@{{.Image}}@{{.Ports}}" | paste -sd '$' -)" && \
    //    echo -n "^" && \
    //    echo -n "$(docker images --format "{{.ID}}@{{.Repository}}@{{.Size}}@{{.CreatedAt}}" | paste -sd '$' -)" && \
    //    echo -n "^" && \
    //    echo -n "$(docker network ls --format "{{.Name}}" | xargs -I {} docker network inspect {} --format "{{.Name}}@{{range .IPAM.Config}}{{.Subnet}}{{end}}@{{range .IPAM.Config}}{{.Gateway}}{{end}}@{{.Created}}" | paste -sd'$' -)" && \
    //    echo -n "^" && \
    //    echo -n "$(docker volume ls --format "{{.Name}}" | xargs -I {} docker volume inspect {} --format "{{.Name}}@{{.Mountpoint}}@{{.CreatedAt}}" | paste -sd'$' -)"
    /**
     * 获取Docker信息
     */
    @Log
    @PostMapping("/docker/info")
    public Result dockerInfo(String sshKey, Integer type) {
        String errorMsg = "获取Docker信息失败";
        String successMsg = "获取Docker信息成功";

        SSHClient ssh = SSHUtil.getSSHClient(sshKey);
        if (ssh == null) {
            return Result.error(ResultCodeEnum.SSH_NOT_EXIST.getState(), "连接断开，" + errorMsg);
        }
        StringBuilder info = new StringBuilder();
        String command = DOCKER_INFO_CMD[type];
        try {
            int exitStatus = SSHUtil.executeCommand(ssh, command, info);
            if (exitStatus != 0) return Result.error(errorMsg);
        } catch (Exception e) {
            LogUtil.logException(this.getClass(), e);
            return Result.error(errorMsg);
        }

        return Result.success(200, successMsg, info.toString());
    }

    /**
     * Docker删除
     */
    @Log
    @PostMapping("/docker/delete")
    public Result dockerDelete(String sshKey, Integer type, String items) {
        String errorMsg = "删除失败";
        String successMsg = "删除成功";

        SSHClient ssh = SSHUtil.getSSHClient(sshKey);
        if (ssh == null) {
            return Result.error(ResultCodeEnum.SSH_NOT_EXIST.getState(), "连接断开，" + errorMsg);
        }
        String command = DOCKER_DELETE_CMD[type] + items;
        try {
            int exitStatus = SSHUtil.executeCommand(ssh, command, null);
            if (exitStatus != 0) return Result.error(errorMsg);
        } catch (Exception e) {
            LogUtil.logException(this.getClass(), e);
            return Result.error(errorMsg);
        }

        return Result.success(200, successMsg, null);
    }


    /**
     * Docker容器操作
     */
    @Log
    @PostMapping("/docker/container")
    public Result dockerContainer(String sshKey, Integer type, String items) {
        String errorMsg = "操作失败";
        String successMsg = "操作成功";

        SSHClient ssh = SSHUtil.getSSHClient(sshKey);
        if (ssh == null) {
            return Result.error(ResultCodeEnum.SSH_NOT_EXIST.getState(), "连接断开，" + errorMsg);
        }
        StringBuilder container = new StringBuilder();
        String command = DOCKER_CONTAINER_CMD[type] + items;
        try {
            int exitStatus = SSHUtil.executeCommand(ssh, command, container);
            if (exitStatus != 0) return Result.error(errorMsg);
        } catch (Exception e) {
            LogUtil.logException(this.getClass(), e);
            return Result.error(errorMsg);
        }

        return Result.success(200, successMsg, container.toString());
    }

}
