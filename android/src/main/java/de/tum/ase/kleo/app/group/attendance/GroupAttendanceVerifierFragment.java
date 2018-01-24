package de.tum.ase.kleo.app.group.attendance;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;
import org.web3j.tx.ManagedTransaction;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import de.tum.ase.kleo.android.BuildConfig;
import de.tum.ase.kleo.android.R;
import de.tum.ase.kleo.app.KleoApplication;
import de.tum.ase.kleo.app.client.BackendClient;
import de.tum.ase.kleo.app.client.GroupsApi;
import de.tum.ase.kleo.app.client.UsersApi;
import de.tum.ase.kleo.app.client.dto.GroupDTO;
import de.tum.ase.kleo.app.client.dto.SessionDTO;
import de.tum.ase.kleo.app.client.dto.UserDTO;
import de.tum.ase.kleo.app.support.ui.ArrayAdapterItem;
import de.tum.ase.kleo.app.support.ui.ReactiveLayoutFragment;
import de.tum.ase.kleo.ethereum.AttendanceTracker;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static de.tum.ase.kleo.app.support.DateTimeFormatters.simpleTime;
import static de.tum.ase.kleo.app.support.ethereum.AndroidWalletUtils.loadCredentials;
import static de.tum.ase.kleo.app.support.ui.ArrayAdapterItem.getSelectedItemValue;
import static java.util.stream.Collectors.toList;

public class GroupAttendanceVerifierFragment extends ReactiveLayoutFragment {

    private static final int READ_WALLET_FILE_REQUEST_CODE = 42;

    private GroupsApi groupsApi;
    private Uri walletFile;
    private Web3j web3j;
    private UsersApi usersApi;

    public GroupAttendanceVerifierFragment() {
        super(R.layout.fragment_group_attendance_verifier);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final BackendClient backendClient =
                ((KleoApplication) getActivity().getApplication()).backendClient();

        groupsApi = backendClient.as(GroupsApi.class);
        usersApi = backendClient.as(UsersApi.class);
        web3j = Web3jFactory.build(new HttpService(BuildConfig.ETHEREUM_INFURA));
    }

    @Override
    protected void onCreateLayout(View view, Bundle state) {
        final Spinner groupSpinner = view.findViewById(R.id.group_attendance_verifier_group_chooser);
        final Spinner sessionSpinner = view.findViewById(R.id.group_attendance_verifier_session_chooser);
        final Spinner userSpinner = view.findViewById(R.id.group_attendance_verifier_user_chooser);
        final Button findBtn = view.findViewById(R.id.group_attendance_verifier_find_tx_btn);
        final Button walletFileFindBtn = view.findViewById(R.id.group_attendance_verifier_wallet_chooser_btn);

        groupSpinner.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                populateGroupChooser(groupSpinner);
            }

            @Override
            public void onViewDetachedFromWindow(View v) {}
        });

        userSpinner.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                populateUserChooser(userSpinner);
            }

            @Override
            public void onViewDetachedFromWindow(View v) {}
        });

        groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final Optional<GroupDTO> selectedGroupOpt
                        = getSelectedItemValue(groupSpinner, GroupDTO.class);

                selectedGroupOpt.ifPresent(group
                        -> populateSessionChooser(sessionSpinner, group.getSessions()));

                hideSessionValidationResulst();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        findBtn.setOnClickListener(v -> {
            final Optional<SessionDTO> selectedSessionOpt
                    = getSelectedItemValue(sessionSpinner, SessionDTO.class);

            final Optional<UserDTO> selectedUserOpt
                    = getSelectedItemValue(userSpinner, UserDTO.class);

            if (selectedSessionOpt.isPresent() && selectedUserOpt.isPresent()) {
                verifySession(selectedSessionOpt.get().getId(), selectedUserOpt.get().getId());
            }
        });

        walletFileFindBtn.setOnClickListener(v -> performWalletFileSearch());
    }

    @Override
    protected void onFragmentCreated(View view, Bundle state) {
        setBlockchainExternalLink(BuildConfig.ETHEREUM_ATTENDANCE_TRACKER_CONTRACT_ADDRESS,
                BuildConfig.ETHEREUM_ATTENDANCE_TRACKER_CONTRACT_URL);
    }

    private void performWalletFileSearch() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, READ_WALLET_FILE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == READ_WALLET_FILE_REQUEST_CODE) {
            loadWalletFile(resultCode, resultData);
        }
    }

    public void loadWalletFile(int resultCode, Intent resultData) {
        if (resultCode == Activity.RESULT_OK && resultData != null) {
            walletFile = resultData.getData();
        } else {
            walletFile = null;
        }

        hideSessionValidationResulst();
    }

    private void populateGroupChooser(Spinner groupSpinner) {
        final Disposable groupsReq = groupsApi.getGroups()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(groups -> {
                    if (groups == null) {
                        groupSpinner.setAdapter(null);
                        return;
                    }

                    final List<ArrayAdapterItem<GroupDTO>> groupAdapterItems = groups.stream()
                            .map(group -> ArrayAdapterItem.of(group.getName(), group))
                            .collect(toList());

                    final ArrayAdapter<ArrayAdapterItem<GroupDTO>> adapter
                            = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item,
                                groupAdapterItems);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    groupSpinner.setAdapter(adapter);
                }, this::showError);

        disposeOnDestroy(groupsReq);
    }

    private void populateSessionChooser(Spinner sessionSpinner, List<SessionDTO> sessions) {
        if (sessions == null) {
            sessionSpinner.setAdapter(null);
            return;
        }
        final List<ArrayAdapterItem<SessionDTO>> groupAdapterItems = sessions.stream()
                .map(session -> {
                    final String sessionInterval = simpleTime(session.getBegins())
                            + " - " + simpleTime(session.getEnds());
                    return ArrayAdapterItem.of(sessionInterval, session);
                })
                .collect(toList());

        final ArrayAdapter<ArrayAdapterItem<SessionDTO>> adapter
                = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item,
                groupAdapterItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        sessionSpinner.setAdapter(adapter);
    }

    private void populateUserChooser(Spinner userSpinner) {
        final Disposable groupsReq = usersApi.getUsers()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(users -> {
                    if (users == null) {
                        userSpinner.setAdapter(null);
                        return;
                    }

                    final List<ArrayAdapterItem<UserDTO>> groupAdapterItems = users.stream()
                            .map(user -> ArrayAdapterItem.of(user.getName(), user))
                            .collect(toList());

                    final ArrayAdapter<ArrayAdapterItem<UserDTO>> adapter
                            = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item,
                            groupAdapterItems);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    userSpinner.setAdapter(adapter);
                }, this::showError);

        disposeOnDestroy(groupsReq);
    }

    private void verifySession(String sessionId, String studentId) {
        try (InputStream walletFileStream = getContext().getContentResolver().openInputStream(walletFile)) {
            final Credentials credentials = loadCredentials(getWalletPassword(), walletFileStream);

            final AttendanceTracker attendanceTracker
                    = AttendanceTracker.load(BuildConfig.ETHEREUM_ATTENDANCE_TRACKER_CONTRACT_ADDRESS,
                    web3j, credentials, ManagedTransaction.GAS_PRICE, Contract.GAS_LIMIT);

            attendanceTracker.hasAttented(sessionId, studentId).observable()
                    .subscribeOn(rx.schedulers.Schedulers.io())
                    .observeOn(rx.android.schedulers.AndroidSchedulers.mainThread())
                    .doOnSubscribe(this::showSessionValidationLoadingCircle)
                    .doOnCompleted(this::hideSessionValidationLoadingCircle)
                    .subscribe(hasAttended -> {
                        if (hasAttended) {
                            showSessionIsValidResult();
                        } else {
                            showSessionIsNotValidResult();
                        }
                    });
        } catch (FileNotFoundException e) {
            Toast.makeText(getContext(), "Invalid path to wallet file", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(getContext(), "Failed to load wallet file", Toast.LENGTH_LONG).show();
        } catch (CipherException e) {
            Toast.makeText(getContext(), "Failed to decrypt wallet file", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getWalletPassword() {
        final TextView pass = getView().findViewById(R.id.group_attendance_verifier_wallet_password_input);
        return pass.getText().toString();
    }

    private void showSessionValidationLoadingCircle() {
        getView().findViewById(R.id.group_attendance_verifier_progressbar)
                .setVisibility(View.VISIBLE);
    }

    private void hideSessionValidationLoadingCircle() {
        getView().findViewById(R.id.group_attendance_verifier_progressbar)
                .setVisibility(View.INVISIBLE);
    }

    private void hideSessionValidationResulst() {
        hideSessionIsNotValidResult();
        hideSessionIsValidResult();
    }

    private void showSessionIsValidResult() {
        hideSessionIsNotValidResult();

        getView().findViewById(R.id.group_attendance_verifier_result_txok_txt)
                .setVisibility(View.VISIBLE);
    }

    private void hideSessionIsValidResult() {
        getView().findViewById(R.id.group_attendance_verifier_result_txok_txt)
                .setVisibility(View.INVISIBLE);
    }

    private void showSessionIsNotValidResult() {
        hideSessionIsValidResult();
        getView().findViewById(R.id.group_attendance_verifier_result_notx_txt)
                .setVisibility(View.VISIBLE);
    }

    private void hideSessionIsNotValidResult() {
        getView().findViewById(R.id.group_attendance_verifier_result_notx_txt)
                .setVisibility(View.INVISIBLE);
    }

    private void setBlockchainExternalLink(String txHash, String link) {
        final TextView linkTxt = getView().findViewById(R.id.group_attendance_verifier_contract_link_txt);
        linkTxt.setText(Html.fromHtml("<a href=\""+ link + "\">" + txHash + "</a>"));
        linkTxt.setLinkTextColor(Color.BLUE);
        linkTxt.setClickable(true);
        linkTxt.setMovementMethod (LinkMovementMethod.getInstance());
    }

    private void showError(Throwable e) {
        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
    }
}
