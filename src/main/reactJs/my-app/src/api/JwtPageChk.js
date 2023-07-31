import { useSnackbar } from "notistack";
import axiosIns from "./JwtConfig";
import {jwtHandleError} from "./JwtHandleError";
import ToastAlert from "./ToastAlert";

export const useJwtPageChk = () => {
    const {enqueueSnackbar} = useSnackbar();
    const toastAlert = ToastAlert(enqueueSnackbar);

    const JwtPageChk = async (navi, url) => {
        try {
            const res = await axiosIns({
                method: 'post',
                url: '/api/member/D1/check',
            });
            if (res.status === 200) {
                navi(url);
            }
        } catch(error) {
            jwtHandleError(error, toastAlert);
        }
    }

    return JwtPageChk;
}