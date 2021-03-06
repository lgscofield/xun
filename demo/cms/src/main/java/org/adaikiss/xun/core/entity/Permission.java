package org.adaikiss.xun.core.entity;

import java.util.Iterator;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

import org.adaikiss.xun.core.util.Constant;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 权限 hlw
 * 
 */
@Entity
@Table(name = Constant.TABLE_PREFIX + "permission")
@IdClass(PermissionPK.class)
@SuppressWarnings("serial")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE)
@JsonInclude(Include.NON_EMPTY)
public class Permission extends WildcardPermission {
	public static final String ALL = WILDCARD_TOKEN;
	public static final String SUPER = "*:*:*";
	/**
	 * 权限的操作领域
	 */
	@Size(max = 20)
	@JsonProperty
	private String domain;
	/**
	 * 权限的操作类型
	 */
	@Size(max = 20)
	@JsonProperty
	private String actions = ALL;
	/**
	 * 权限的具体操作对象
	 */
	@Size(max = 20)
	@JsonProperty
	private String targets = ALL;

	/**
	 * 权限描述
	 */
	@Size(max = 100)
	@JsonProperty
	private String description;

	public Permission() {
		this(ALL, ALL, ALL);
	}

	public Permission(String perm) {
		if (StringUtils.isBlank(perm)) {
			this.setParts(domain, actions, targets);
			return;
		}
		String[] perms = perm.trim().split(PART_DIVIDER_TOKEN);
		if (perms.length == 1) {
			if (StringUtils.isNotBlank(perms[0])) {
				this.domain = perms[0].trim();
			}
			this.setParts(domain, actions, targets);
			return;
		}
		if (perms.length == 2) {
			if (StringUtils.isNotBlank(perms[0])) {
				this.domain = perms[0].trim();
			}
			if (StringUtils.isNotBlank(perms[1])) {
				this.actions = perms[1].trim();
			}
			this.setParts(domain, actions, targets);
			return;
		}
		if (perms.length == 3) {
			if (StringUtils.isNotBlank(perms[0])) {
				this.domain = perms[0].trim();
			}
			if (StringUtils.isNotBlank(perms[1])) {
				this.actions = perms[1].trim();
			}
			if (StringUtils.isNotBlank(perms[2])) {
				this.targets = perms[2].trim();
			}
			this.setParts(domain, actions, targets);
		}
	}

	public Permission(String domain, String actions) {
		if (StringUtils.isNotEmpty(domain)) {
			this.domain = domain.trim();
		}
		if (StringUtils.isNotEmpty(actions)) {
			this.actions = actions.trim();
		}
		this.setParts(this.domain, this.actions, targets);
	}

	public Permission(String domain, String actions, String targets) {
		if (StringUtils.isNotEmpty(domain)) {
			this.domain = domain.trim();
		}
		if (StringUtils.isNotEmpty(actions)) {
			this.actions = actions.trim();
		}
		if (StringUtils.isNotEmpty(targets)) {
			this.targets = targets.trim();
		}
		this.setParts(this.domain, this.actions, this.targets);
	}

	@Id
	@Column(nullable = false)
	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
		if (StringUtils.isBlank(this.domain)) {
			this.domain = ALL;
		}
		this.setParts(this.domain, actions, targets);
	}

	@Id
	public String getActions() {
		return actions;
	}

	public void setActions(String actions) {
		this.actions = actions;
		if (StringUtils.isEmpty(this.actions)) {
			this.actions = ALL;
		}
		if (null != domain) {
			this.setParts(domain, actions, targets);
		}
	}

	@Id
	public String getTargets() {
		return targets;
	}

	public void setTargets(String targets) {
		this.targets = targets;
		if (StringUtils.isEmpty(this.targets)) {
			this.targets = ALL;
		}
		if (null != domain) {
			this.setParts(domain, actions, targets);
		}
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Transient
	protected void setParts(String domain, String actions, String targets) {
		if (StringUtils.isEmpty(domain)) {
			throw new IllegalArgumentException(
					"domain argument cannot be null or empty.");
		}
		StringBuilder sb = new StringBuilder(domain);

		if (StringUtils.isEmpty(actions)) {
			if (!StringUtils.isEmpty(targets)) {
				sb.append(PART_DIVIDER_TOKEN).append(WILDCARD_TOKEN);
			}
		} else {
			sb.append(PART_DIVIDER_TOKEN).append(actions);
		}
		if (targets != null) {
			sb.append(PART_DIVIDER_TOKEN).append(targets);
		}
		setParts(sb.toString());
	}

	@Transient
	protected String toString(Set<String> set) {
		StringBuilder sb = new StringBuilder();
		Iterator<String> it = set.iterator();
		while (it.hasNext()) {
			sb.append(it.next());
			if (it.hasNext())
				sb.append(SUBPART_DIVIDER_TOKEN);
		}
		return sb.toString();
	}

	@JsonProperty("text")
	@Transient
	public String getText() {
		return new StringBuilder().append(this.domain)
				.append(PART_DIVIDER_TOKEN).append(this.actions)
				.append(PART_DIVIDER_TOKEN).append(this.targets).toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj == null || !(obj instanceof Permission))
			return false;
		Permission perm = (Permission) obj;
		return StringUtils.equals(domain, perm.getDomain())
				&& StringUtils.equals(actions, perm.getActions())
				&& StringUtils.equals(targets, perm.getTargets());
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = result * 37
				+ (null == domain ? "".hashCode() : domain.hashCode());
		result = result * 37 + actions.hashCode();
		result = result * 37 + targets.hashCode();
		return result;
	}
}
